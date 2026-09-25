package com.naze.expense.ui.history

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FilterListOff
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.naze.expense.domain.model.TransactionType
import com.naze.expense.ui.components.CategoryIcon
import com.naze.expense.ui.components.EmptyState
import com.naze.expense.ui.components.TransactionListItem
import com.naze.expense.ui.theme.formatAmount
import com.naze.expense.ui.theme.formatDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onTransactionClick: (Long) -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val filter = state.filter
    val currency = state.settings.currencyCode
    var showFilterSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Riwayat") },
                actions = {
                    if (filter.isActive) {
                        IconButton(onClick = viewModel::clear) {
                            Icon(Icons.Filled.FilterListOff, contentDescription = "Hapus filter")
                        }
                    }
                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(Icons.Filled.FilterList, contentDescription = "Filter")
                    }
                }
            )
        },
    ) { padding ->
        if (state.groups.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyState(
                    if (filter.isActive) "Tidak ada transaksi sesuai filter"
                    else "Belum ada transaksi. Tekan + untuk mulai mencatat!"
                )
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(padding)) {
                if (filter.isActive) {
                    item { FilterSummary(state, viewModel, Modifier.padding(horizontal = 16.dp)) }
                }
                items(state.groups, key = { it.transactions.first().id }) { group ->
                    Column {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                group.dayHeader,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f),
                            )
                            if (group.dayIncome > 0) {
                                Text(
                                    "+" + formatAmount(group.dayIncome, currency),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.secondary,
                                )
                            }
                            if (group.dayExpense > 0) {
                                Text(
                                    "  -" + formatAmount(group.dayExpense, currency),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.tertiary,
                                )
                            }
                        }
                        group.transactions.forEach { tx ->
                            TransactionListItem(
                                transaction = tx,
                                currency = currency,
                                onClick = { onTransactionClick(tx.id) },
                            )
                        }
                    }
                }
                item { Spacer(Modifier.height(96.dp)) }
            }
        }
    }

    if (showFilterSheet) {
        FilterSheet(
            state = state,
            onDismiss = { showFilterSheet = false },
            onType = viewModel::setType,
            onCategory = viewModel::setCategory,
            onDateRange = viewModel::setDateRange,
            onClear = viewModel::clear,
        )
    }
}

@Composable
private fun FilterSummary(
    state: HistoryState,
    viewModel: HistoryViewModel,
    modifier: Modifier = Modifier,
) {
    val labels = buildList {
        state.filter.type?.let { add(if (it == TransactionType.INCOME) "Pemasukan" else "Pengeluaran") }
        state.filter.categoryId?.let { id ->
            add(state.categories.find { it.id == id }?.name ?: "Kategori")
        }
        state.filter.startDate?.let { add("Dari " + formatDate(it)) }
        state.filter.endDate?.let { add("Sampai " + formatDate(it - 1)) }
    }
    Row(
        modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        labels.forEach { label ->
            FilterChip(selected = true, onClick = viewModel::clear, label = { Text(label) })
        }
        Text(
            state.totalCount.toString() + " transaksi",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterVertically),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSheet(
    state: HistoryState,
    onDismiss: () -> Unit,
    onType: (TransactionType?) -> Unit,
    onCategory: (Long?) -> Unit,
    onDateRange: (Long?, Long?) -> Unit,
    onClear: () -> Unit,
) {
    var pickStart by remember { mutableStateOf(true) }
    var showDatePicker by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Filter Riwayat", style = MaterialTheme.typography.titleLarge)

            Text("Jenis", style = MaterialTheme.typography.titleMedium)
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = state.filter.type == null,
                    onClick = { onType(null) },
                    shape = SegmentedButtonDefaults.itemShape(0, 3),
                ) { Text("Semua") }
                SegmentedButton(
                    selected = state.filter.type == TransactionType.EXPENSE,
                    onClick = { onType(TransactionType.EXPENSE) },
                    shape = SegmentedButtonDefaults.itemShape(1, 3),
                ) { Text("Keluar") }
                SegmentedButton(
                    selected = state.filter.type == TransactionType.INCOME,
                    onClick = { onType(TransactionType.INCOME) },
                    shape = SegmentedButtonDefaults.itemShape(2, 3),
                ) { Text("Masuk") }
            }

            Text("Kategori", style = MaterialTheme.typography.titleMedium)
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = state.filter.categoryId == null,
                    onClick = { onCategory(null) },
                    label = { Text("Semua") },
                )
                state.categories.forEach { cat ->
                    FilterChip(
                        selected = state.filter.categoryId == cat.id,
                        onClick = { onCategory(cat.id) },
                        label = { Text(cat.name) },
                        leadingIcon = { CategoryIcon(cat, size = 20) },
                    )
                }
            }

            Text("Rentang Tanggal (opsional)", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { pickStart = true; showDatePicker = true }) {
                    Text(state.filter.startDate?.let { "Dari: " + formatDate(it) } ?: "Dari tanggal")
                }
                OutlinedButton(onClick = { pickStart = false; showDatePicker = true }) {
                    Text(state.filter.endDate?.let { "Sampai: " + formatDate(it - 1) } ?: "Sampai tanggal")
                }
            }

            if (state.filter.startDate != null || state.filter.endDate != null) {
                TextButton(onClick = { onDateRange(null, null) }) { Text("Hapus rentang tanggal") }
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(onClick = { onClear(); onDismiss() }, modifier = Modifier.weight(1f)) {
                    Text("Reset")
                }
                Button(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Terapkan") }
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showDatePicker) {
        val dateState = rememberDatePickerState(
            initialSelectedDateMillis = if (pickStart) state.filter.startDate
            else state.filter.endDate?.let { it - 1 }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let { picked ->
                        if (pickStart) {
                            val start = picked
                            val end = state.filter.endDate
                            val fixedEnd = if (end != null && end <= start) start + 86_400_000 else end
                            onDateRange(start, fixedEnd)
                        } else {
                            val endExclusive = picked + 86_400_000
                            val start = state.filter.startDate
                            val fixedStart =
                                if (start != null && start >= endExclusive) endExclusive - 86_400_000 else start
                            onDateRange(fixedStart, endExclusive)
                        }
                    }
                    showDatePicker = false
                }) { Text("Pilih") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Batal") } },
        ) {
            DatePicker(state = dateState)
        }
    }
}
