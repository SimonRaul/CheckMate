package com.checkmate.app.data.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Junction table: which mates had which item (many-to-many).
 * An item shared by several mates has one row per mate and its price is divided equally.
 */
@Entity(
    tableName = "item_assignments",
    primaryKeys = ["item_id", "mate_id"],
    foreignKeys = [
        ForeignKey(
            entity = ItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["item_id"],
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
data class ItemAssignmentEntity(
    @ColumnInfo(name = "item_id") val itemId: Long,
    @ColumnInfo(name = "mate_id") val mateId: Long,
)
