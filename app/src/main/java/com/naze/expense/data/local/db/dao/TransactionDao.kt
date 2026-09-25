package com.naze.expense.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.naze.expense.data.local.entity.TransactionEntity
import com.naze.expense.data.local.relation.TransactionWithCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    // Riwayat: terbaru dulu
    @androidx.room.Transaction
    @Query("SELECT * FROM transactions ORDER BY date DESC, id DESC")
    fun observeAllWithCategory(): Flow<List<TransactionWithCategory>>

    // Dashboard: agregasi bulan berjalan
    @Query(
        "SELECT COALESCE(SUM(amount), 0) FROM transactions " +
            "WHERE type = 'INCOME' AND date BETWEEN :startMillis AND :endMillis"
    )
    fun observeTotalIncomeBetween(startMillis: Long, endMillis: Long): Flow<Long>

    @Query(
        "SELECT COALESCE(SUM(amount), 0) FROM transactions " +
            "WHERE type = 'EXPENSE' AND date BETWEEN :startMillis AND :endMillis"
    )
    fun observeTotalExpenseBetween(startMillis: Long, endMillis: Long): Flow<Long>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'INCOME'")
    fun observeTotalIncomeAll(): Flow<Long>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'EXPENSE'")
    fun observeTotalExpenseAll(): Flow<Long>

    // Pengeluaran per kategori dalam rentang tanggal (untuk budget & statistik)
    @Query(
        "SELECT categoryId, SUM(amount) AS total FROM transactions " +
            "WHERE type = 'EXPENSE' AND date BETWEEN :startMillis AND :endMillis " +
            "GROUP BY categoryId"
    )
    fun observeExpenseByCategory(startMillis: Long, endMillis: Long): Flow<List<CategoryTotal>>

    // Ringkasan harian
    @Query(
        "SELECT date, SUM(CASE WHEN type='INCOME' THEN amount ELSE 0 END) AS income, " +
            "SUM(CASE WHEN type='EXPENSE' THEN amount ELSE 0 END) AS expense " +
            "FROM transactions WHERE date BETWEEN :startMillis AND :endMillis " +
            "GROUP BY date ORDER BY date ASC"
    )
    fun observeDailyTotals(startMillis: Long, endMillis: Long): Flow<List<DailyTotal>>

    @androidx.room.Transaction
    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getByIdWithCategory(id: Long): TransactionWithCategory?

    @Insert
    suspend fun insert(transaction: TransactionEntity): Long

    @Insert
    suspend fun insertAll(transactions: List<TransactionEntity>)

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions")
    suspend fun getAll(): List<TransactionEntity>

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()
}

data class CategoryTotal(val categoryId: Long, val total: Long)

data class DailyTotal(val date: Long, val income: Long, val expense: Long)
