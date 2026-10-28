package com.checkmate.app.data.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** A line item on a receipt. Deleted together with its receipt. */
@Entity(
    tableName = "items",
    foreignKeys = [
        ForeignKey(
            entity = ReceiptEntity::class,
            parentColumns = ["id"],
            childColumns = ["receipt_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("receipt_id")],
)
data class ItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "receipt_id") val receiptId: Long,
    val name: String,
    val price: Double,
)
