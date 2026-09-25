package com.naze.expense.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.naze.expense.data.local.db.dao.BudgetDao
import com.naze.expense.data.local.db.dao.CategoryDao
import com.naze.expense.data.local.db.dao.TransactionDao
import com.naze.expense.data.local.entity.BudgetEntity
import com.naze.expense.data.local.entity.CategoryEntity
import com.naze.expense.data.local.entity.TransactionEntity

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        BudgetEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class NazeDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        @Volatile
        private var INSTANCE: NazeDatabase? = null

        fun get(context: Context): NazeDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    NazeDatabase::class.java,
                    "naze_expense.db",
                ).build().also { INSTANCE = it }
            }
    }
}
