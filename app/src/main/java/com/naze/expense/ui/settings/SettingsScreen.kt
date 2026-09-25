package com.naze.expense.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.naze.expense.domain.model.Category
import com.naze.expense.domain.model.ThemeMode
import com.naze.expense.domain.model.TransactionType
import com.naze.expense.ui.components.CategoryIcon
import com.naze.expense.ui.theme.formatAmount
import kotlinx.coroutines.launch

private val CURRENCIES = listOf("IDR", "USD", "EUR", "SGD", "MYR", "JPY")

private val PALETTE = listOf(
    "#F4511E", "#1E88E5", "#8E24AA", "#00897B", "#6D4C41",
    "#E53935", "#3949AB", "#757575", "#43A047", "#FB8C00",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val state by viewModel.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var showCategoryDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<Category?>(null) }

    // Backup: simpan file JSON (SAF, 100% lokal)
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                runCatching {
                    val text = viewModel.exportJson()
                    context.contentResolver.openOutputStream(uri)?.use { os ->
                        os.write(text.toByteArray())
                    } ?: error("Tidak bisa membuka file")
                }.onSuccess { viewModel.notify("Backup berhasil disimpan") }
                    .onFailure { viewModel.notify("Gagal menyimpan backup") }
            }
        }
    }

    // Restore: pilih file JSON lalu impor (SAF)
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                runCatching {
                    val text = context.contentResolver.openInputStream(uri)?.use { input ->
                        input.readBytes().decodeToString()
                    } ?: error("File kosong")
                    viewModel.importJson(text)
                }
            }
        }
    }

    LaunchedEffect(state.snackbar) {
        state.snackbar?.let { snackbar.showSnackbar(it); viewModel.consumeSnackbar() }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Setelan") }) },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Mata uang
            item {
                SettingsCard("Mata Uang") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CURRENCIES.forEach { code ->
                            FilterChip(
                                selected = state.settings.currencyCode == code,
                                onClick = { viewModel.setCurrency(code) },
                                label = { Text(formatAmount(1000L, code).split("1")[0] + " " + code) },
                            )
                        }
                    }
                }
            }

            // Tema
            item {
                SettingsCard("Tema") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ThemeMode.entries.forEach { mode ->
                            FilterChip(
                                selected = state.settings.themeMode == mode,
                                onClick = { viewModel.setTheme(mode) },
                                label = {
                                    Text(
                                        when (mode) {
                                            ThemeMode.LIGHT -> "Terang"
                                            ThemeMode.DARK -> "Gelap"
                                            ThemeMode.SYSTEM -> "Sistem"
                                        }
                                    )
                                },
                            )
                        }
                    }
                }
            }

            // Kelola kategori
            item {
                SettingsCard("Kelola Kategori") {
                    state.categories.forEach { category ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            CategoryIcon(category, size = 32)
                            Column(Modifier.weight(1f)) {
                                Text(category.name, style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    if (category.type == TransactionType.EXPENSE) "Pengeluaran"
                                    else "Pemasukan",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            IconButton(onClick = {
                                editingCategory = category
                                showCategoryDialog = true
                            }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Ubah kategori")
                            }
                            IconButton(onClick = { viewModel.deleteCategory(category) }) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = "Hapus kategori",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            editingCategory = null
                            showCategoryDialog = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Tambah Kategori") }
                }
            }

            // Backup & restore lokal
            item {
                SettingsCard("Backup & Restore") {
                    Text(
                        "Simpan seluruh data (transaksi, kategori, budget) ke file JSON di perangkat, atau pulihkan dari file backup.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = {
                                exportLauncher.launch("naze-expense-backup.json")
                            },
                            modifier = Modifier.weight(1f),
                        ) { Text("Ekspor") }
                        OutlinedButton(
                            onClick = {
                                importLauncher.launch(arrayOf("application/json"))
                            },
                            modifier = Modifier.weight(1f),
                        ) { Text("Impor") }
                    }
                }
            }
            item { Spacer(Modifier.height(32.dp)) }
        }
    }

    if (showCategoryDialog) {
        CategoryDialog(
            existing = editingCategory,
            onDismiss = { showCategoryDialog = false },
            onSave = { name, type ->
                val color = PALETTE[(state.categories.size) % PALETTE.size]
                viewModel.saveCategory(editingCategory, name, type, color)
                showCategoryDialog = false
            },
        )
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun CategoryDialog(
    existing: Category?,
    onDismiss: () -> Unit,
    onSave: (name: String, type: TransactionType) -> Unit,
) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var type by remember {
        mutableStateOf(existing?.type ?: TransactionType.EXPENSE)
    }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Tambah Kategori" else "Ubah Kategori") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama kategori") },
                    singleLine = true,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = type == TransactionType.EXPENSE,
                        onClick = { type = TransactionType.EXPENSE },
                        label = { Text("Pengeluaran") },
                    )
                    FilterChip(
                        selected = type == TransactionType.INCOME,
                        onClick = { type = TransactionType.INCOME },
                        label = { Text("Pemasukan") },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(name, type) }) { Text("Simpan") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        },
    )
}
