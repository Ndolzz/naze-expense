package com.naze.expense.domain.model

enum class TransactionType {
    INCOME, EXPENSE;

    val isExpense: Boolean get() = this == EXPENSE
}
