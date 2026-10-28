package com.checkmate.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.checkmate.app.data.db.entities.MateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MateDao {
    /** "You" first, then mates in the order they were added. */
    @Query("SELECT * FROM mates ORDER BY is_self DESC, id ASC")
    fun observeAll(): Flow<List<MateEntity>>

    @Query("SELECT COUNT(*) FROM mates")
    suspend fun count(): Int

    @Insert
    suspend fun insert(mate: MateEntity): Long
}
