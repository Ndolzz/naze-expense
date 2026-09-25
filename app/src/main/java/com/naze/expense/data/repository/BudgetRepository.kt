package com.naze.expense.data.repository

import com.naze.expense.data.local.db.dao.BudgetDao
import com.naze.expense.data.local.entity.BudgetEntity
import com.naze.expense.data.local.relation.BudgetWithCategory
import com.naze.expense.domain.model.Budget
import com.naze.expense.domain.model.Category
import com.naze.expense.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BudgetRepository(private val budgetDao: BudgetDao) {

    fun observeAll(): Flow<List<Budget>> =
        budgetDao.observeAllWithCategory().map { list -> list.map { it.toDomain() } }

    suspend fun upsert(categoryId: Long, monthlyLimit: Long) {
        val existing = budgetDao.getByCategory(categoryId)
        if (existing == null) {
            budgetDao.upsert(BudgetEntity(categoryId = categoryId, monthlyLimit = monthlyLimit))
        } else {
            budgetDao.upsert(existing.copy(monthlyLimit = monthlyLimit))
        }
    }

    suspend fun deleteByCategory(categoryId: Long) = budgetDao.deleteByCategory(categoryId)

    suspend fun getAll(): List<Budget> = budgetDao.getAll().map {
        Budget(id = it.id, categoryId = it.categoryId, monthlyLimit = it.monthlyLimit)
    }

    suspend fun replaceAll(budgets: List<Budget>) {
        budgets.forEach { budgetDao.upsert(BudgetEntity(id = 0, categoryId = it.categoryId, monthlyLimit = it.monthlyLimit)) }
    }

    private fun BudgetWithCategory.toDomain() = Budget(
        id = budget.id,
        categoryId = budget.categoryId,
        monthlyLimit = budget.monthlyLimit,
        category = category?.let {
            Category(
                id = it.id,
                name = it.name,
                icon = it.icon,
                colorHex = it.colorHex,
                isDefault = it.isDefault,
                type = TransactionType.valueOf(it.type),
            )
        },
    )
}
