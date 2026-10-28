package com.checkmate.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.checkmate.app.data.db.entities.ItemAssignmentEntity
import com.checkmate.app.data.db.entities.ItemEntity
import com.checkmate.app.data.db.entities.ReceiptEntity
import com.checkmate.app.data.db.entities.ReceiptMateEntity
import com.checkmate.app.data.db.entities.ReceiptWithDetails
import kotlinx.coroutines.flow.Flow

/** An item to insert together with the ids of the mates who had it. */
data class ItemWithMates(val item: ItemEntity, val mateIds: Collection<Long>)

@Dao
abstract class ReceiptDao {
    /** Dashboard "Recent activity": re-emits whenever any of the queried tables change. */
    @Transaction
    @Query("SELECT * FROM receipts ORDER BY created_at DESC")
    abstract fun observeRecent(): Flow<List<ReceiptWithDetails>>

    @Query("SELECT COUNT(*) FROM receipts")
    abstract suspend fun count(): Int

    @Insert
    abstract suspend fun insertReceipt(receipt: ReceiptEntity): Long

    @Insert
    abstract suspend fun insertItem(item: ItemEntity): Long

    @Insert
    abstract suspend fun insertAssignments(assignments: List<ItemAssignmentEntity>)

    @Insert
    abstract suspend fun insertShares(shares: List<ReceiptMateEntity>)

    /**
     * Saves a whole split in one transaction: the receipt, its items, who had each item and
     * every mate's share. `receiptId` on the passed items/shares is ignored and filled in here.
     */
    @Transaction
    open suspend fun saveFullReceipt(
        receipt: ReceiptEntity,
        items: List<ItemWithMates>,
        shares: List<ReceiptMateEntity>,
    ): Long {
        val receiptId = insertReceipt(receipt)
        for ((item, mateIds) in items) {
            val itemId = insertItem(item.copy(receiptId = receiptId))
            insertAssignments(mateIds.map { ItemAssignmentEntity(itemId = itemId, mateId = it) })
        }
        insertShares(shares.map { it.copy(receiptId = receiptId) })
        return receiptId
    }
}
