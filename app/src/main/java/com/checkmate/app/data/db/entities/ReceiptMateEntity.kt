package com.checkmate.app.data.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/** Junction table: a mate's computed share of a receipt (tip included) and whether it's settled. */
@Entity(
    tableName = "receipt_mates",
    primaryKeys = ["receipt_id", "mate_id"],
    foreignKeys = [
        ForeignKey(
            entity = ReceiptEntity::class,
            parentColumns = ["id"],
            childColumns = ["receipt_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = MateEntity::class,
            parentColumns = ["id"],
            childColumns = ["mate_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("mate_id")],
)
data class ReceiptMateEntity(
    @ColumnInfo(name = "receipt_id") val receiptId: Long,
    @ColumnInfo(name = "mate_id") val mateId: Long,
    @ColumnInfo(name = "amount_owed") val amountOwed: Double,
    @ColumnInfo(name = "is_settled") val isSettled: Boolean = false,
)
