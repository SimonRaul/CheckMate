package com.checkmate.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.checkmate.app.data.CheckMateRepository
import com.checkmate.app.data.db.entities.ReceiptWithDetails
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class DashboardViewModel(repository: CheckMateRepository) : ViewModel() {
    /** Saved receipts, newest first, loaded from Room. `null` while the first query runs. */
    val receipts: StateFlow<List<ReceiptWithDetails>?> = repository.recentReceipts
        .stateIn<List<ReceiptWithDetails>?>(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
