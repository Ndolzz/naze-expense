package com.naze.expense.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.naze.expense.data.local.entity.SavingDepositEntity
import com.naze.expense.data.local.entity.SavingGoalEntity
import com.naze.expense.data.local.relation.GoalWithDeposits
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingGoalDao {

    @Transaction
    @Query("SELECT * FROM saving_goals ORDER BY createdAt DESC")
    fun observeAllWithDeposits(): Flow<List<GoalWithDeposits>>

    @Insert
    suspend fun insertGoal(goal: SavingGoalEntity): Long

    @Update
    suspend fun updateGoal(goal: SavingGoalEntity)

    @Delete
    suspend fun deleteGoal(goal: SavingGoalEntity)

    @Insert
    suspend fun insertDeposit(deposit: SavingDepositEntity): Long

    @Delete
    suspend fun deleteDeposit(deposit: SavingDepositEntity)

    @Query("SELECT * FROM saving_goals")
    suspend fun getAllGoals(): List<SavingGoalEntity>

    @Query("SELECT * FROM saving_deposits")
    suspend fun getAllDeposits(): List<SavingDepositEntity>

    @Query("DELETE FROM saving_deposits")
    suspend fun deleteAllDeposits()

    @Query("DELETE FROM saving_goals")
    suspend fun deleteAllGoals()
}
