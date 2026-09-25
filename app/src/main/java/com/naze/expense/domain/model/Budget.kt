package com.naze.expense.domain.model

data class Budget(
    val id: Long = 0L,
    val categoryId: Long,
    val monthlyLimit: Long,      // satuan terkecil (rupiah)
    val category: Category? = null,
)
