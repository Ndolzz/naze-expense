package com.naze.expense.ui.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naze.expense.AppContainer
import com.naze.expense.data.local.db.dao.CategoryTotal
import com.naze.expense.data.preferences.UserSettings
import com.naze.expense.domain.model.Budget
import com.naze.expense.domain.model.Category
import com.naze.expense.ui.theme.currentMonthRange
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class BudgetItem(
    val budget: Budget,
    val spent: Long,            // pengeluaran bulan berjalan untuk kategori ini
    val remaining: Long,
    val progress: Float,        // 0..1+ (bisa > 1 jika over budget)
)

data class BudgetState(
    val items: List<BudgetItem> = emptyList(),
    val totalBudget: Long = 0L,
    val totalSpent: Long = 0L,
    val totalRemaining: Long = 0L,
    val expenseCategories: List<Category> = emptyList(), // kategori tanpa budget (untuk tambah)
    val settings: UserSettings = UserSettings(),
    val snackbar: String? = null,
)

class BudgetViewModel(container: AppContainer) : ViewModel() {

    private val budgetRepo = container.budgetRepository
    private val catRepo = container.categoryRepository
    private val txRepo = container.transactionRepository
    private val settingsRepo = container.settingsRepository

    private val (monthStart, monthEnd) = currentMonthRange()
    private val _snackbar = MutableStateFlow<String?>(null)

    val state: StateFlow<BudgetState> = combine(
        budgetRepo.observeAll(),
        txRepo.observeExpenseByCategory(monthStart, monthEnd),
        catRepo.observeByType(com.naze.expense.domain.model.TransactionType.EXPENSE),
        settingsRepo.settings,
        _snackbar,
    ) { budgets, totals, expenseCats, settings, snackbar ->
        val spentMap = totals.associate { it.categoryId to it.total }

        val items = budgets
            .filter { it.category?.type == com.naze.expense.domain.model.TransactionType.EXPENSE }
            .map { b ->
                val spent = spentMap[b.categoryId] ?: 0L
                BudgetItem(
                    budget = b,
                    spent = spent,
                    remaining = b.monthlyLimit - spent,
                    progress = if (b.monthlyLimit > 0) spent.toFloat() / b.monthlyLimit else 0f,
                )
            }
            .sortedByDescending { it.progress }

        val budgetedCatIds = budgets.map { it.categoryId }.toSet()

        BudgetState(
            items = items,
            totalBudget = items.sumOf { it.budget.monthlyLimit },
            totalSpent = items.sumOf { it.spent },
            totalRemaining = items.sumOf { it.remaining },
            expenseCategories = expenseCats.filter { it.id !in budgetedCatIds },
            settings = settings,
            snackbar = snackbar,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BudgetState())

    fun setBudget(categoryId: Long, monthlyLimit: Long) {
        if (monthlyLimit <= 0) {
            _snackbar.value = "Nominal budget harus lebih dari 0"
            return
        }
        viewModelScope.launch {
            budgetRepo.upsert(categoryId, monthlyLimit)
            _snackbar.value = "Budget disimpan"
        }
    }

    fun deleteBudget(categoryId: Long) {
        viewModelScope.launch {
            budgetRepo.deleteByCategory(categoryId)
            _snackbar.value = "Budget dihapus"
        }
    }

    fun consumeSnackbar() { _snackbar.value = null }
}
