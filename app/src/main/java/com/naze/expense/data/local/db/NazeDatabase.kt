package com.naze.expense.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.naze.expense.data.local.db.dao.BudgetDao
import com.naze.expense.data.local.db.dao.CategoryDao
import com.naze.expense.data.local.db.dao.SavingGoalDao
import com.naze.expense.data.local.db.dao.TransactionDao
import com.naze.expense.data.local.entity.BudgetEntity
import com.naze.expense.data.local.entity.CategoryEntity
import com.naze.expense.data.local.entity.SavingDepositEntity
import com.naze.expense.data.local.entity.SavingGoalEntity
import com.naze.expense.data.local.entity.TransactionEntity

@Database(
    entities = [
        TransactionEntity::class,
        CategoryEntity::class,
        BudgetEntity::class,
        SavingGoalEntity::class,
        SavingDepositEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
abstract class NazeDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao
    abstract fun savingGoalDao(): SavingGoalDao

    companion object {
        /** v1 -> v2: tabel tabungan (goals + setoran). */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `saving_goals` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`name` TEXT NOT NULL, " +
                        "`targetAmount` INTEGER NOT NULL, " +
                        "`deadline` INTEGER NOT NULL, " +
                        "`photoPath` TEXT, " +
                        "`createdAt` INTEGER NOT NULL)"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `saving_deposits` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`goalId` INTEGER NOT NULL, " +
                        "`amount` INTEGER NOT NULL, " +
                        "`date` INTEGER NOT NULL, " +
                        "FOREIGN KEY(`goalId`) REFERENCES `saving_goals`(`id`) " +
                        "ON UPDATE NO ACTION ON DELETE CASCADE)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_saving_deposits_goalId` " +
                        "ON `saving_deposits` (`goalId`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_saving_deposits_date` " +
                        "ON `saving_deposits` (`date`)"
                )
            }
        }

        @Volatile
        private var INSTANCE: NazeDatabase? = null

        fun get(context: Context): NazeDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    NazeDatabase::class.java,
                    "naze_expense.db",
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
