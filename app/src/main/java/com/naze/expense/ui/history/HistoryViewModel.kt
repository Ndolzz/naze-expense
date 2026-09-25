package com.naze.expense.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naze.expense.AppContainer
import com.naze.expense.data.preferences.UserSettings
import com.naze.expense.domain.model.Category
import com.naze.expense.domain.model.Transaction
import com.naze.expense.domain.model.TransactionType
import com.naze.expense.ui.theme.formatDayHeader
import com.naze.expense.ui.theme.startOfDay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** Filter riwayat. null = tanpa filter. */
data class HistoryFilter(
    val type: TransactionType? = null,
    val categoryId: Long? = null,
    val startDate: Long? = null,
    val endDate: Long? = null,
) {
    val isActive: Boolean
        get() = type != null || categoryId != null || startDate != null || endDate != null
}

data class HistoryGroup(
    val dayHeader: String,
    val dayExpense: Long,
    val dayIncome: Long,
    val transactions: List<Transaction>,
)

data class HistoryState(
    val filter: HistoryFilter = HistoryFilter(),
    val categories: List<Category> = emptyList(),
    val groups: List<HistoryGroup> = emptyList(),
    val totalCount: Int = 0,
    val settings: UserSettings = UserSettings(),
)

class HistoryViewModel(container: AppContainer) : ViewModel() {

    private val txRepo = container.transactionRepository
    private val catRepo = container.categoryRepository
    private val settingsRepo = container.settingsRepository

    private val _filter = MutableStateFlow(HistoryFilter())
    val filter: StateFlow<HistoryFilter> = _filter

    val state: StateFlow<HistoryState> = combine(
        txRepo.observeAll(),
        catRepo.observeAll(),
        settingsRepo.settings,
        _filter,
    ) { txs, cats, settings, filter ->
        val filtered = txs.filter { tx ->
            val typeOk = filter.type == null || tx.type == filter.type
            val catOk = filter.categoryId == null || tx.categoryId == filter.categoryId
            val startOk = filter.startDate == null || tx.date >= filter.startDate
            val endOk = filter.endDate == null || tx.date < filter.endDate
            typeOk && catOk && startOk && endOk
        }

        val groups = filtered
            .groupBy { startOfDay(it.date) }
            .map { (_, list) ->
                HistoryGroup(
                    dayHeader = formatDayHeader(list.first().date),
                    dayIncome = list.filter { it.isIncome }.sumOf { it.amount },
                    dayExpense = list.filter { !it.isIncome }.sumOf { it.amount },
                    transactions = list,
                )
            }
            .sortedByDescending { it.transactions.first().date }

        HistoryState(
            filter = filter,
            categories = cats,
            groups = groups,
            totalCount = filtered.size,
            settings = settings,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoryState())

    fun setType(type: TransactionType?) { _filter.value = _filter.value.copy(type = type) }
    fun setCategory(categoryId: Long?) { _filter.value = _filter.value.copy(categoryId = categoryId) }
    fun setDateRange(start: Long?, endExclusive: Long?) {
        _filter.value = _filter.value.copy(startDate = start, endDate = endExclusive)
    }
    fun clear() { _filter.value = HistoryFilter() }
}
