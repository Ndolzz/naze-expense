package com.naze.expense.domain.model

data class Transaction(
    val id: Long = 0L,
    val amount: Long,           // satuan terkecil (rupiah), bukan pecahan
    val type: TransactionType,
    val categoryId: Long,
    val note: String? = null,
    val date: Long,             // epoch millis
    val category: Category? = null, // hasil join, untuk tampilan
) {
    val isIncome: Boolean get() = type == TransactionType.INCOME
}
