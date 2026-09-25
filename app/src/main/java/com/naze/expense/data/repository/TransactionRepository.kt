package com.naze.expense.data.repository

import com.naze.expense.data.local.db.dao.CategoryDao
import com.naze.expense.data.local.db.dao.CategoryTotal
import com.naze.expense.data.local.db.dao.DailyTotal
import com.naze.expense.data.local.db.dao.TransactionDao
import com.naze.expense.data.local.entity.TransactionEntity
import com.naze.expense.data.local.relation.TransactionWithCategory
import com.naze.expense.domain.model.Category
import com.naze.expense.domain.model.Transaction
import com.naze.expense.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
) {

    fun observeAll(): Flow<List<Transaction>> =
        transactionDao.observeAllWithCategory().map { list -> list.map { it.toDomain() } }

    fun observeTotalIncomeBetween(start: Long, end: Long): Flow<Long> =
        transactionDao.observeTotalIncomeBetween(start, end)

    fun observeTotalExpenseBetween(start: Long, end: Long): Flow<Long> =
        transactionDao.observeTotalExpenseBetween(start, end)

    fun observeTotalIncomeAll(): Flow<Long> = transactionDao.observeTotalIncomeAll()

    fun observeTotalExpenseAll(): Flow<Long> = transactionDao.observeTotalExpenseAll()

    fun observeExpenseByCategory(start: Long, end: Long): Flow<List<CategoryTotal>> =
        transactionDao.observeExpenseByCategory(start, end)

    fun observeDailyTotals(start: Long, end: Long): Flow<List<DailyTotal>> =
        transactionDao.observeDailyTotals(start, end)

    suspend fun getById(id: Long): Transaction? =
        transactionDao.getByIdWithCategory(id)?.toDomain()

    suspend fun add(transaction: Transaction): Long =
        transactionDao.insert(transaction.toEntity())

    suspend fun update(transaction: Transaction) =
        transactionDao.update(transaction.toEntity())

    suspend fun delete(transaction: Transaction) =
        transactionDao.delete(transaction.toEntity())

    // Backup/restore
    suspend fun getAllEntities(): List<TransactionEntity> = transactionDao.getAll()

    suspend fun replaceAll(transactions: List<TransactionEntity>) {
        transactionDao.deleteAll()
        transactionDao.insertAll(transactions)
    }

    private fun TransactionWithCategory.toDomain() = Transaction(
        id = transaction.id,
        amount = transaction.amount,
        type = TransactionType.valueOf(transaction.type),
        categoryId = transaction.categoryId,
        note = transaction.note,
        date = transaction.date,
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

    private fun Transaction.toEntity() = TransactionEntity(
        id = id,
        amount = amount,
        type = type.name,
        categoryId = categoryId,
        note = note?.trim()?.takeIf { it.isNotEmpty() },
        date = date,
    )
}
