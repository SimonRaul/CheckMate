package com.checkmate.app.data.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** A person bills are split with. `color` is the ARGB avatar colour; `isSelf` marks "You". */
@Entity(tableName = "mates")
data class MateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val color: Long,
    @ColumnInfo(name = "is_self") val isSelf: Boolean = false,
)
