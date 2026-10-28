package com.checkmate.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.checkmate.app.data.db.dao.MateDao
import com.checkmate.app.data.db.dao.ReceiptDao
import com.checkmate.app.data.db.entities.ItemAssignmentEntity
import com.checkmate.app.data.db.entities.ItemEntity
import com.checkmate.app.data.db.entities.MateEntity
import com.checkmate.app.data.db.entities.ReceiptEntity
import com.checkmate.app.data.db.entities.ReceiptMateEntity

/** The local SQLite database (`checkmate.db`). See PLAN.md for the ER diagram. */
@Database(
    entities = [
        ReceiptEntity::class,
        ItemEntity::class,
        MateEntity::class,
        ItemAssignmentEntity::class,
        ReceiptMateEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun receiptDao(): ReceiptDao
    abstract fun mateDao(): MateDao

    companion object {
        private const val NAME = "checkmate.db"

        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, NAME).build()
    }
}
