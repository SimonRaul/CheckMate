package com.checkmate.app.data.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** A split bill. `subtotal` is the sum of its items; `total` includes the tip. */
@Entity(tableName = "receipts")
data class ReceiptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "merchant_name") val merchantName: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    val subtotal: Double,
    @ColumnInfo(name = "tip_percent") val tipPercent: Int,
    val total: Double,
)
