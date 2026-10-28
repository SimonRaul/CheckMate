package com.checkmate.app.data.db.entities

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

/** A receipt with its items and the mates it was split between (via `receipt_mates`). */
data class ReceiptWithDetails(
    @Embedded val receipt: ReceiptEntity,
    @Relation(parentColumn = "id", entityColumn = "receipt_id")
    val items: List<ItemEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = ReceiptMateEntity::class,
            parentColumn = "receipt_id",
            entityColumn = "mate_id",
        ),
    )
    val mates: List<MateEntity>,
)
