package com.naze.expense.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.naze.expense.ui.components.AmountCard
import com.naze.expense.ui.components.EmptyState
import com.naze.expense.ui.components.TransactionListItem
import com.naze.expense.ui.theme.formatAmount
import com.naze.expense.ui.theme.monthLabel

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onTransactionClick: (Long) -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val currency = state.settings.currencyCode

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            Column(Modifier.padding(horizontal = 16.dp)) {
                Spacer(Modifier.height(8.dp))
                Text("Saldo saat ini", style = MaterialTheme.typography.bodyMedium)
                Text(
                    formatAmount(state.balance, currency),
                    style = MaterialTheme.typography.displayLarge,
                    color = if (state.balance < 0) MaterialTheme.colorScheme.tertiary
                    else MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AmountCard(
                    title = "Pemasukan " + monthLabel(),
                    amount = formatAmount(state.incomeThisMonth, currency),
                    accent = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f),
                )
                AmountCard(
                    title = "Pengeluaran " + monthLabel(),
                    amount = formatAmount(state.expenseThisMonth, currency),
                    accent = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                ),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Ringkasan " + monthLabel(), style = MaterialTheme.typography.titleMedium)
                    val net = state.incomeThisMonth - state.expenseThisMonth
                    Text(
                        if (net >= 0)
                            "Kamu surplus " + formatAmount(net, currency) + " bulan ini. Pertahankan!"
                        else
                            "Kamu defisit " + formatAmount(-net, currency) + " bulan ini. Hati-hati ya",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        }
        item {
            Text(
                "Transaksi Terakhir",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }
        if (state.recentTransactions.isEmpty()) {
            item { EmptyState("Belum ada transaksi. Tekan + untuk mulai mencatat!") }
        } else {
            items(state.recentTransactions, key = { it.id }) { tx ->
                TransactionListItem(
                    transaction = tx,
                    currency = currency,
                    onClick = { onTransactionClick(tx.id) },
                )
            }
        }
        item { Spacer(Modifier.height(96.dp)) }
    }
}
