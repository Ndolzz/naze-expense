package com.naze.expense.ui.statistics

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.naze.expense.ui.components.CategoryIcon
import com.naze.expense.ui.components.EmptyState
import com.naze.expense.ui.components.parseColor
import com.naze.expense.ui.theme.formatAmount
import com.naze.expense.ui.theme.formatDateShort

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(viewModel: StatisticsViewModel) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Statistik") }) },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            // Pilih periode: Harian / Mingguan / Bulanan
            TabRow(selectedTabIndex = state.period.ordinal) {
                StatPeriod.entries.forEach { p ->
                    Tab(
                        selected = state.period == p,
                        onClick = { viewModel.selectPeriod(p) },
                        text = { Text(p.label) },
                    )
                }
            }

            LazyColumn(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Ringkasan periode
                item {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        SummaryCard(
                            title = "Pemasukan",
                            amount = formatAmount(state.income, state.settings.currencyCode),
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f),
                        )
                        SummaryCard(
                            title = "Pengeluaran",
                            amount = formatAmount(state.expense, state.settings.currencyCode),
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
                item {
                    Text(
                        if (state.net >= 0) "Selisih: +" + formatAmount(state.net, state.settings.currencyCode)
                        else "Selisih: -" + formatAmount(-state.net, state.settings.currencyCode),
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (state.net >= 0) MaterialTheme.colorScheme.secondary
                        else MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }

                // Distribusi pengeluaran per kategori
                item {
                    Text(
                        "Pengeluaran per Kategori",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp),
                    )
                }
                if (state.slices.isEmpty()) {
                    item {
                        EmptyState("Belum ada pengeluaran di periode " + state.period.label.lowercase())
                    }
                } else {
                    items(state.slices, key = { it.category?.id ?: 0L }) { slice ->
                        CategorySliceRow(slice, state.settings.currencyCode)
                    }
                }

                // Tren harian (bar chart sederhana)
                if (state.period != StatPeriod.DAILY && state.dailyTotals.isNotEmpty()) {
                    item {
                        Text(
                            "Tren Harian",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(start = 16.dp, top = 16.dp),
                        )
                    }
                    item { DailyBarChart(state.dailyTotals) }
                }
                item { Spacer(Modifier.height(96.dp)) }
            }
        }
    }
}

@Composable
private fun SummaryCard(title: String, amount: String, color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = color)
            Text(amount, style = MaterialTheme.typography.titleLarge, maxLines = 1)
        }
    }
}

@Composable
private fun CategorySliceRow(slice: CategorySlice, currency: String) {
    val category = slice.category
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (category != null) CategoryIcon(category)
                Column(Modifier.weight(1f)) {
                    Text(
                        category?.name ?: "Tanpa kategori",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(formatAmount(slice.total, currency), style = MaterialTheme.typography.titleMedium)
                    Text(
                        (slice.fraction * 100).toInt().toString() + "%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            LinearProgressIndicator(
                progress = { slice.fraction.coerceIn(0f, 1f) },
                color = if (category != null) parseColor(category.colorHex) else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** Bar chart harian sederhana: tinggi bar proporsional dengan pengeluaran hari itu. */
@Composable
private fun DailyBarChart(daily: List<com.naze.expense.data.local.db.dao.DailyTotal>) {
    val maxExpense = (daily.maxOfOrNull { it.expense } ?: 0L).coerceAtLeast(1L)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                daily.forEach { d ->
                    Column(
                        Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        // Bar pengeluaran
                        val h = (d.expense.toFloat() / maxExpense * 100f).toInt().coerceAtLeast(2)
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(h.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                    RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp),
                                ),
                        )
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                daily.forEach { d ->
                    Text(
                        formatDateShort(d.date),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                    )
                }
            }
        }
    }
}
