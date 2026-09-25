package com.naze.expense.ui.budget

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.naze.expense.ui.components.CategoryIcon
import com.naze.expense.ui.components.EmptyState
import com.naze.expense.ui.theme.formatAmount
import com.naze.expense.ui.theme.monthLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(viewModel: BudgetViewModel) {
    val state by viewModel.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var showAddSheet by remember { mutableStateOf(false) }

    LaunchedEffect(state.snackbar) {
        state.snackbar?.let { snackbar.showSnackbar(it); viewModel.consumeSnackbar() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Budget") },
                actions = {
                    if (state.expenseCategories.isNotEmpty()) {
                        IconButton(onClick = { showAddSheet = true }) {
                            Icon(Icons.Filled.Add, contentDescription = "Tambah budget")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        if (state.items.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyState("Belum ada budget. Tekan + untuk set budget per kategori.")
            }
        } else {
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                // Ringkasan total
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                        ),
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                "Total Budget ${monthLabel()}",
                                style = MaterialTheme.typography.titleMedium,
                            )
                            val currency = state.settings.currencyCode
                            Text(
                                "${formatAmount(state.totalSpent, currency)} dari ${formatAmount(state.totalBudget, currency)}",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                            Text(
                                if (state.totalRemaining >= 0)
                                    "Sisa: ${formatAmount(state.totalRemaining, currency)}"
                                else
                                    "Over budget: ${formatAmount(-state.totalRemaining, currency)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (state.totalRemaining >= 0)
                                    MaterialTheme.colorScheme.secondary
                                else MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.padding(top = 2.dp),
                            )
                        }
                    }
                }
                // Daftar budget per kategori
                items(state.items, key = { it.budget.categoryId }) { item ->
                    BudgetCard(item, state.settings.currencyCode, onDelete = {
                        viewModel.deleteBudget(item.budget.categoryId)
                    })
                }
                item { Spacer(Modifier.height(96.dp)) }
            }
        }
    }

    if (showAddSheet) {
        AddBudgetSheet(
            categories = state.expenseCategories,
            currency = state.settings.currencyCode,
            onDismiss = { showAddSheet = false },
            onSave = { categoryId, limit ->
                viewModel.setBudget(categoryId, limit)
                showAddSheet = false
            },
        )
    }
}

@Composable
private fun BudgetCard(
    item: BudgetItem,
    currency: String,
    onDelete: () -> Unit,
) {
    val category = item.budget.category ?: return
    val over = item.remaining < 0
    val nearLimit = !over && item.progress >= 0.8f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CategoryIcon(category)
                Column(Modifier.weight(1f)) {
                    Text(category.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Limit: ${formatAmount(item.budget.monthlyLimit, currency)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Hapus budget",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Progress bar pengeluaran vs budget
            LinearProgressIndicator(
                progress = { item.progress.coerceAtMost(1f) },
                color = when {
                    over -> MaterialTheme.colorScheme.tertiary
                    nearLimit -> androidx.compose.ui.graphics.Color(0xFFFF9800)
                    else -> MaterialTheme.colorScheme.primary
                },
                trackColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth(),
            )

            Row(Modifier.fillMaxWidth(), alignment = Alignment.CenterVertically) {
                Text(
                    "Terpakai: ${formatAmount(item.spent, currency)}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    if (over) "Lewat ${formatAmount(-item.remaining, currency)}"
                    else "Sisa ${formatAmount(item.remaining, currency)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = when {
                        over -> MaterialTheme.colorScheme.tertiary
                        nearLimit -> androidx.compose.ui.graphics.Color(0xFFFF9800)
                        else -> MaterialTheme.colorScheme.secondary
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddBudgetSheet(
    categories: List<com.naze.expense.domain.model.Category>,
    currency: String,
    onDismiss: () -> Unit,
    onSave: (categoryId: Long, monthlyLimit: Long) -> Unit,
) {
    var selectedCategoryId by remember { mutableStateOf(categories.firstOrNull()?.id) }
    var amountText by remember { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Tambah Budget", style = MaterialTheme.typography.titleLarge)

            // Pilih kategori
            Text("Kategori", style = MaterialTheme.typography.titleMedium)
            androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(((categories.size + 2) / 3 * 48).dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(categories.size) { i ->
                    val cat = categories[i]
                    FilterChip(
                        selected = selectedCategoryId == cat.id,
                        onClick = { selectedCategoryId = cat.id },
                        label = { Text(cat.name, maxLines = 1) },
                        leadingIcon = { CategoryIcon(cat, size = 20) },
                    )
                }
            }

            OutlinedTextField(
                value = amountText,
                onValueChange = { if (it.all { c -> c.isDigit() } && it.length <= 15) amountText = it },
                label = { Text("Batas budget per bulan") },
                prefix = { Text("Rp") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                textStyle = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.fillMaxWidth(),
            )

            Button(
                onClick = {
                    val amount = amountText.toLongOrNull() ?: 0L
                    val catId = selectedCategoryId
                    if (catId != null && amount > 0) {
                        onSave(catId, amount)
                    }
                },
                enabled = selectedCategoryId != null && amountText.toLongOrNull()?.let { it > 0 } == true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) { Text("Simpan Budget") }

            Spacer(Modifier.height(24.dp))
        }
    }
}
