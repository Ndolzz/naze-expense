package com.naze.expense.data.repository

import com.naze.expense.data.local.db.dao.CategoryDao
import com.naze.expense.data.local.entity.CategoryEntity
import com.naze.expense.domain.model.Category
import com.naze.expense.domain.model.DEFAULT_CATEGORIES
import com.naze.expense.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoryRepository(private val dao: CategoryDao) {

    fun observeAll(): Flow<List<Category>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    fun observeByType(type: TransactionType): Flow<List<Category>> =
        dao.observeByType(type.name).map { list -> list.map { it.toDomain() } }

    suspend fun getById(id: Long): Category? = dao.getById(id)?.toDomain()

    suspend fun getAll(): List<Category> = dao.getAll().map { it.toDomain() }

    suspend fun add(category: Category): Long =
        dao.insert(category.toEntity())

    suspend fun update(category: Category) = dao.update(category.toEntity())

    /** Return false jika kategori masih dipakai transaksi (tidak boleh dihapus). */
    suspend fun delete(category: Category): Boolean {
        if (dao.countTransactionsFor(category.id) > 0) return false
        dao.delete(category.toEntity())
        return true
    }

    /** Seed kategori bawaan saat pertama kali. */
    suspend fun seedDefaultsIfEmpty() {
        if (dao.count() == 0) {
            dao.insertAll(DEFAULT_CATEGORIES.map { it.toEntity() })
        }
    }

    private fun CategoryEntity.toDomain() = Category(
        id = id,
        name = name,
        icon = icon,
        colorHex = colorHex,
        isDefault = isDefault,
        type = TransactionType.valueOf(type),
    )

    private fun Category.toEntity() = CategoryEntity(
        id = id,
        name = name.trim(),
        icon = icon,
        colorHex = colorHex,
        isDefault = isDefault,
        type = type.name,
    )
}
