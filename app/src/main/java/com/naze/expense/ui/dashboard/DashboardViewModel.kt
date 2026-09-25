package com.naze.expense.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naze.expense.AppContainer
import com.naze.expense.data.preferences.UserSettings
import com.naze.expense.domain.model.Transaction
import com.naze.expense.ui.theme.currentMonthRange
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class DashboardState(
    val balance: Long = 0L,
    val incomeThisMonth: Long = 0L,
    val expenseThisMonth: Long = 0L,
    val recentTransactions: List<Transaction> = emptyList(),
    val settings: UserSettings = UserSettings(),
)

class DashboardViewModel(container: AppContainer) : ViewModel() {

    private val repo = container.transactionRepository
    private val settingsRepo = container.settingsRepository
    private val (monthStart, monthEnd) = currentMonthRange()

    val state: StateFlow<DashboardState> = combine(
        repo.observeTotalIncomeAll(),
        repo.observeTotalExpenseAll(),
        repo.observeTotalIncomeBetween(monthStart, monthEnd),
        repo.observeTotalExpenseBetween(monthStart, monthEnd),
        repo.observeAll(),
        settingsRepo.settings,
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val incomeAll = values[0] as Long
        val expenseAll = values[1] as Long
        val incomeMonth = values[2] as Long
        val expenseMonth = values[3] as Long
        val all = values[4] as List<Transaction>
        val settings = values[5] as UserSettings
        DashboardState(
            balance = incomeAll - expenseAll,
            incomeThisMonth = incomeMonth,
            expenseThisMonth = expenseMonth,
            recentTransactions = all.take(5),
            settings = settings,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardState())
}
