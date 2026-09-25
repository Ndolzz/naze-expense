package com.naze.expense.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.naze.expense.data.local.entity.BudgetEntity
import com.naze.expense.data.local.relation.BudgetWithCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Transaction
    @Query("SELECT * FROM budgets")
    fun observeAllWithCategory(): Flow<List<BudgetWithCategory>>

    @Query("SELECT * FROM budgets WHERE categoryId = :categoryId")
    suspend fun getByCategory(categoryId: Long): BudgetEntity?

    @Query("SELECT * FROM budgets")
    suspend fun getAll(): List<BudgetEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(budget: BudgetEntity)

    @Query("DELETE FROM budgets WHERE categoryId = :categoryId")
    suspend fun deleteByCategory(categoryId: Long)
}
