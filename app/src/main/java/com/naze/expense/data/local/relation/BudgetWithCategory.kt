package com.naze.expense.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.naze.expense.data.local.entity.BudgetEntity
import com.naze.expense.data.local.entity.CategoryEntity

data class BudgetWithCategory(
    @Embedded val budget: BudgetEntity,
    @Relation(parentColumn = "categoryId", entityColumn = "id")
    val category: CategoryEntity?,
)
