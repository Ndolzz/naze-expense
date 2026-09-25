package com.naze.expense.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.naze.expense.data.local.entity.CategoryEntity
import com.naze.expense.data.local.entity.TransactionEntity

data class TransactionWithCategory(
    @Embedded val transaction: TransactionEntity,
    @Relation(parentColumn = "categoryId", entityColumn = "id")
    val category: CategoryEntity?,
)
