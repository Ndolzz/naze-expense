package com.naze.expense.data.repository

import com.naze.expense.data.local.db.dao.SavingGoalDao
import com.naze.expense.data.local.entity.SavingDepositEntity
import com.naze.expense.data.local.entity.SavingGoalEntity
import com.naze.expense.data.local.relation.GoalWithDeposits
import kotlinx.coroutines.flow.Flow

class SavingGoalRepository(private val dao: SavingGoalDao) {

    fun observeAll(): Flow<List<GoalWithDeposits>> = dao.observeAllWithDeposits()

    suspend fun add(goal: SavingGoalEntity): Long = dao.insertGoal(goal)

    suspend fun update(goal: SavingGoalEntity) = dao.updateGoal(goal)

    suspend fun delete(goal: SavingGoalEntity) = dao.deleteGoal(goal)

    suspend fun deposit(deposit: SavingDepositEntity): Long = dao.insertDeposit(deposit)
}
