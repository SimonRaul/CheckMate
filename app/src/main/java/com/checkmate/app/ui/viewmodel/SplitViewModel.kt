package com.checkmate.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.checkmate.app.data.CheckMateRepository
import com.checkmate.app.data.MockData
import com.checkmate.app.data.NewItem
import com.checkmate.app.data.db.entities.MateEntity
import com.checkmate.app.domain.MateShare
import com.checkmate.app.domain.SplitCalculator
import com.checkmate.app.domain.SplitLine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale

/** A receipt line being edited. `priceText` is kept as typed so the field never fights the user. */
data class DraftItem(
    val key: Long,
    val name: String,
    val priceText: String,
    val mateIds: Set<Long> = emptySet(),
) {
    val price: Double? get() = priceText.replace(',', '.').toDoubleOrNull()
    val isValid: Boolean get() = name.isNotBlank() && (price ?: 0.0) > 0.0
}

data class SplitUiState(
    val merchantName: String = "",
    val items: List<DraftItem> = emptyList(),
    val scannedCount: Int = 0,
    val selectedMateId: Long? = null,
    val tipPercent: Int = 0,
    val settledMateIds: Set<Long> = emptySet(),
    val isSaving: Boolean = false,
) {
    val subtotal: Double get() = items.sumOf { it.price ?: 0.0 }
    val canConfirmItems: Boolean get() = items.isNotEmpty() && items.all { it.isValid }
    val unassignedCount: Int get() = items.count { it.mateIds.isEmpty() }
}

/** One row of the "Who owes what" list. */
data class ShareRow(val mate: MateEntity, val share: MateShare)

/**
 * Holds the receipt being split while the user moves through Scan → Edit → Assign → Settle Up,
 * and saves it to the database at the end.
 */
class SplitViewModel(private val repository: CheckMateRepository) : ViewModel() {

    private val _state = MutableStateFlow(SplitUiState())
    val state: StateFlow<SplitUiState> = _state.asStateFlow()

    val mates: StateFlow<List<MateEntity>> = repository.mates
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val shares: StateFlow<List<ShareRow>> = combine(_state, mates) { state, mates ->
        val byMate = computeShares(state).associateBy { it.mateId }
        mates.mapNotNull { mate -> byMate[mate.id]?.let { ShareRow(mate, it) } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private var nextKey = 0L

    // --- Scan ---

    /** Simulates OCR: fills the draft with the items printed on the mock receipt. */
    fun startNewScan() {
        val items = MockData.scannedItems.map { (name, price) ->
            DraftItem(key = nextKey++, name = name, priceText = String.format(Locale.US, "%.2f", price))
        }
        _state.value = SplitUiState(
            merchantName = MockData.SCANNED_MERCHANT,
            items = items,
            scannedCount = items.size,
            selectedMateId = mates.value.firstOrNull { it.isSelf }?.id,
        )
    }

    // --- Edit receipt ---

    fun updateMerchant(name: String) = _state.update { it.copy(merchantName = name) }

    fun updateItemName(key: Long, name: String) = updateItem(key) { it.copy(name = name) }

    fun updateItemPrice(key: Long, text: String) {
        if (!PRICE_INPUT.matches(text)) return
        updateItem(key) { it.copy(priceText = text) }
    }

    fun removeItem(key: Long) = _state.update { s -> s.copy(items = s.items.filterNot { it.key == key }) }

    fun addItem() = _state.update { s ->
        s.copy(items = s.items + DraftItem(key = nextKey++, name = "", priceText = ""))
    }

    // --- Assign mates ---

    fun selectMate(mateId: Long) = _state.update { it.copy(selectedMateId = mateId) }

    /** Adds the mate to the item, or removes them if they were already on it. */
    fun toggleAssignment(itemKey: Long, mateId: Long) = updateItem(itemKey) { item ->
        val ids = if (mateId in item.mateIds) item.mateIds - mateId else item.mateIds + mateId
        item.copy(mateIds = ids)
    }

    /** "Split": share the item between all mates, or clear it if it already is. */
    fun toggleSplit(itemKey: Long) {
        val all = mates.value.map { it.id }.toSet()
        updateItem(itemKey) { item ->
            item.copy(mateIds = if (item.mateIds.containsAll(all)) emptySet() else all)
        }
    }

    /** Saves a new mate to the database and selects them. */
    fun addMate(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val id = repository.addMate(name)
            _state.update { it.copy(selectedMateId = id) }
        }
    }

    // --- Settle up ---

    fun setTip(percent: Int) = _state.update { it.copy(tipPercent = percent) }

    fun toggleSettled(mateId: Long) = _state.update { s ->
        val ids = if (mateId in s.settledMateIds) s.settledMateIds - mateId else s.settledMateIds + mateId
        s.copy(settledMateIds = ids)
    }

    /** Persists the receipt, its items, assignments and shares in one DB transaction. */
    fun save(onSaved: () -> Unit) {
        val current = _state.value
        if (current.isSaving) return
        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            repository.saveReceipt(
                merchantName = current.merchantName.trim(),
                tipPercent = current.tipPercent,
                items = current.items.map { NewItem(it.name.trim(), it.price ?: 0.0, it.mateIds) },
                shares = computeShares(current),
                settledMateIds = current.settledMateIds,
            )
            _state.update { it.copy(isSaving = false) }
            onSaved()
        }
    }

    private fun computeShares(state: SplitUiState): List<MateShare> = SplitCalculator.split(
        state.items.mapNotNull { item -> item.price?.let { SplitLine(it, item.mateIds) } },
        state.tipPercent,
    )

    private fun updateItem(key: Long, transform: (DraftItem) -> DraftItem) = _state.update { s ->
        s.copy(items = s.items.map { if (it.key == key) transform(it) else it })
    }

    private companion object {
        /** Up to 5 digits, optional '.' or ',' and up to 2 decimals. */
        val PRICE_INPUT = Regex("""^\d{0,5}([.,]\d{0,2})?$""")
    }
}
