package com.checkmate.app.data

import com.checkmate.app.data.db.AppDatabase
import com.checkmate.app.data.db.dao.ItemWithMates
import com.checkmate.app.data.db.entities.ItemEntity
import com.checkmate.app.data.db.entities.MateEntity
import com.checkmate.app.data.db.entities.ReceiptEntity
import com.checkmate.app.data.db.entities.ReceiptMateEntity
import com.checkmate.app.data.db.entities.ReceiptWithDetails
import com.checkmate.app.domain.MateShare
import com.checkmate.app.domain.SplitCalculator
import com.checkmate.app.domain.SplitLine
import com.checkmate.app.ui.theme.MatePalette
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit

/** An item of a receipt being saved, with the mates who had it. */
data class NewItem(val name: String, val price: Double, val mateIds: Set<Long>)

/** Single entry point to the local database for the ViewModels. */
class CheckMateRepository(db: AppDatabase) {
    private val receiptDao = db.receiptDao()
    private val mateDao = db.mateDao()

    val recentReceipts: Flow<List<ReceiptWithDetails>> = receiptDao.observeRecent()
    val mates: Flow<List<MateEntity>> = mateDao.observeAll()

    suspend fun addMate(name: String): Long {
        val color = MatePalette[mateDao.count() % MatePalette.size]
        return mateDao.insert(MateEntity(name = name.trim(), color = color))
    }

    /** Persists a finished split. Returns the new receipt id. */
    suspend fun saveReceipt(
        merchantName: String,
        tipPercent: Int,
        items: List<NewItem>,
        shares: List<MateShare>,
        settledMateIds: Set<Long> = emptySet(),
        createdAt: Long = System.currentTimeMillis(),
    ): Long {
        val receipt = ReceiptEntity(
            merchantName = merchantName.ifBlank { "Receipt" },
            createdAt = createdAt,
            subtotal = shares.sumOf { it.subtotalCents } / 100.0,
            tipPercent = tipPercent,
            total = shares.sumOf { it.totalCents } / 100.0,
        )
        val itemRows = items.map {
            ItemWithMates(ItemEntity(receiptId = 0, name = it.name, price = it.price), it.mateIds)
        }
        val shareRows = shares.map {
            ReceiptMateEntity(
                receiptId = 0,
                mateId = it.mateId,
                amountOwed = it.totalCents / 100.0,
                isSettled = it.mateId in settledMateIds,
            )
        }
        return receiptDao.saveFullReceipt(receipt, itemRows, shareRows)
    }

    /** First launch: create "You" and two mates plus a couple of past receipts. */
    suspend fun seedIfEmpty() {
        if (mateDao.count() > 0) return
        val you = mateDao.insert(MateEntity(name = "You", color = MatePalette[0], isSelf = true))
        val maria = mateDao.insert(MateEntity(name = "Maria", color = MatePalette[1]))
        val john = mateDao.insert(MateEntity(name = "John", color = MatePalette[2]))
        if (receiptDao.count() > 0) return

        val now = System.currentTimeMillis()
        seedReceipt(
            merchant = "Pizza Napoli",
            daysAgo = 2,
            selfId = you,
            now = now,
            tipPercent = 10,
            items = listOf(
                NewItem("Pizza Diavola", 12.50, setOf(you)),
                NewItem("Pizza Quattro Formaggi", 13.00, setOf(maria)),
                NewItem("Calzone", 11.50, setOf(john)),
                NewItem("Lemonade jug", 7.20, setOf(you, maria, john)),
            ),
        )
        seedReceipt(
            merchant = "Campus Coffee",
            daysAgo = 5,
            selfId = you,
            now = now,
            tipPercent = 0,
            items = listOf(
                NewItem("Flat White", 4.20, setOf(you)),
                NewItem("Iced Latte", 4.80, setOf(maria)),
                NewItem("Croissant", 3.10, setOf(you, maria)),
            ),
        )
    }

    private suspend fun seedReceipt(
        merchant: String,
        daysAgo: Long,
        selfId: Long,
        now: Long,
        tipPercent: Int,
        items: List<NewItem>,
    ) {
        val shares = SplitCalculator.split(items.map { SplitLine(it.price, it.mateIds) }, tipPercent)
        saveReceipt(
            merchantName = merchant,
            tipPercent = tipPercent,
            items = items,
            shares = shares,
            settledMateIds = setOf(selfId),
            createdAt = now - TimeUnit.DAYS.toMillis(daysAgo),
        )
    }
}
