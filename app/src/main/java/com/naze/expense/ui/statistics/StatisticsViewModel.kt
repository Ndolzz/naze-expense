package com.naze.expense.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naze.expense.AppContainer
import com.naze.expense.data.local.db.dao.CategoryTotal
import com.naze.expense.data.local.db.dao.DailyTotal
import com.naze.expense.data.preferences.UserSettings
import com.naze.expense.domain.model.Category
import com.naze.expense.ui.theme.currentMonthRange
import com.naze.expense.ui.theme.startOfDay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

enum class StatPeriod(val label: String) {
    DAILY("Harian"),
    WEEKLY("Mingguan"),
    MONTHLY("Bulanan"),
}

/** Potongan pengeluaran per kategori dalam periode terpilih. */
data class CategorySlice(
    val category: Category?,
    val total: Long,
    val fraction: Float, // 0..1 dari total pengeluaran periode
)

data class StatisticsState(
    val period: StatPeriod = StatPeriod.MONTHLY,
    val income: Long = 0L,
    val expense: Long = 0L,
    val net: Long = 0L,
    val dailyTotals: List<DailyTotal> = emptyList(),
    val slices: List<CategorySlice> = emptyList(),
    val settings: UserSettings = UserSettings(),
)

class StatisticsViewModel(container: AppContainer) : ViewModel() {

    private val txRepo = container.transactionRepository
    private val catRepo = container.categoryRepository
    private val settingsRepo = container.settingsRepository

    private val now = System.currentTimeMillis()
    private val _period = MutableStateFlow(StatPeriod.MONTHLY)
    val period: StateFlow<StatPeriod> = _period

    @kotlinx.coroutines.ExperimentalCoroutinesApi
    val state: StateFlow<StatisticsState> = _period.flatMapLatest { p ->
        val (start, end) = rangeFor(p)
        combine(
            txRepo.observeTotalIncomeBetween(start, end),
            txRepo.observeTotalExpenseBetween(start, end),
            txRepo.observeDailyTotals(start, end),
            txRepo.observeExpenseByCategory(start, end),
            catRepo.observeAll(),
        ) { income, expense, daily, totals, categories ->
            val catMap = categories.associateBy { it.id }
            val sliceList = totals
                .sortedByDescending { it.total }
                .map { t ->
                    CategorySlice(
                        category = catMap[t.categoryId],
                        total = t.total,
                        fraction = if (expense > 0) t.total.toFloat() / expense else 0f,
                    )
                }
            StatisticsState(
                period = p,
                income = income,
                expense = expense,
                net = income - expense,
                dailyTotals = daily,
                slices = sliceList,
            )
        }
    }.combine(settingsRepo.settings) { s, settings -> s.copy(settings = settings) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatisticsState())

    fun selectPeriod(p: StatPeriod) { _period.value = p }

    private fun rangeFor(p: StatPeriod): Pair<Long, Long> = when (p) {
        StatPeriod.DAILY -> {
            val start = startOfDay(now)
            start to (start + 86_400_000 - 1)
        }
        StatPeriod.WEEKLY -> {
            val endToday = startOfDay(now) + 86_400_000 - 1
            val cal = Calendar.getInstance().apply {
                timeInMillis = startOfDay(now)
                add(Calendar.DAY_OF_YEAR, -6)
            }
            cal.timeInMillis to endToday
        }
        StatPeriod.MONTHLY -> currentMonthRange()
    }
}
