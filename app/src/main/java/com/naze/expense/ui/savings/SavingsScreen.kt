package com.naze.expense.ui.savings

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snack
barHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.naze.expense.data.local.entity.SavingGoalEntity
import com.naze.expense.ui.theme.formatAmount
import java.text.NumberFormat
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingsScreen(viewModel: SavingsViewModel) {
    val state by viewModel.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    val currency = state.settings.currencyCode
    val context = LocalContext.current

    var showGoalDialog by remember { mutableStateOf(false) }
    var editingGoal by remember { mutableStateOf<SavingGoalEntity?>(null) }

    LaunchedEffect(state.snackbar) {
        state.snackbar?.let { snackbar.showSnackbar(it); viewModel.consumeSnackbar() }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Menabung") }) },
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            FloatingActionButton(onClick = { editingGoal = null; showGoalDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Tambah target")
        
    }
        },
    ) { padding ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Kata penyemangat — berganti setiap hari
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                    ),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(
                            "Semangat hari ini",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            state.quote,
                            style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic),
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }

            if (state.goals.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.Savings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(56.dp),
                            )
                            Text(
                                "Belum ada target menabung.\nTekan + untuk mulai menabung untuk ses
uatu!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 12.dp),
                            )
                        }
                    }
                }
            } else {
                items(state.goals.size, key = { state.goals[it].goal.id }) { i ->
                    GoalCard(
                        ui = state.goals[i],
                        currency = currency,
                        onDeposit = { viewModel.deposit(state.goals[i].goal, it) },
                        onEdit = { editingGoal = state.goals[i].goal; showGoalDialog = true },
                        onDelete = { viewModel.deleteGoal(state.goals[i].goal) },
                    )
                }
            }
            item { Spacer(Modifier.height(96.dp)) }
        }
    }

    if (showGoalDialog) {
        GoalDialog(
            existing = editingGoal,
            onDismiss = { showGoalDialog = false },
            onSave = { name, target, deadline, photo ->
                if (editingGoal == null) {
                    viewModel.addGoal(name, target, deadline, photo, context)
                } else {
                    viewModel.updateGoal(editingGoal!!, name, target, deadline, photo, context)
                }
                showGoalDialog = false
            },
        )
    }
}

@Composable
private fun GoalCard(
    ui: SavingGoalUi,
    currency: String,
    onDeposit: (Long) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    var showCustom by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {


            // Header: foto target + nama + aksi
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ui.goal.photoPath?.let { path ->
                    val bmp = remember(path) {
                        runCatching {
                            BitmapFactory.decodeFile(path)?.asImageBitmap()
                        }.getOrNull()
                    }
                    if (bmp != null) {
                        Image(
                            bitmap = bmp,
                            contentDescription = "Foto target",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(12.dp)),
                        )
                    }
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        ui.goal.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2, overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        if (ui.done) "Target tercapai!"
                        else formatAmount(ui.saved, currency) + " dari " + formatAmount(ui.goal.targetAmount, currency),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onEdit) { Icon(Icons.Filled.Edit, contentDescription = "Ubah") }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Progress
            LinearProgressIndicator(
                progress = { ui.progress },
                modi
fier = Modifier.fillMaxWidth(),
                color = if (ui.done) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
            )

            // Kalkulator hitung mundur
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                ),
            ) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        if (ui.done) "Kamu sudah mencapai target ini!"
                        else "Sisa " + ui.daysLeft + " hari - setor " +
                            formatAmount(ui.perDay, currency) + " per hari",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    if (!ui.done) {
                        Text(
                            "atau " + formatAmount(ui.perWeek, currency) + " per minggu / " +
                                formatAmount(ui.perMonth, currency) + " per bulan",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            // Setor cepat: nominal bebas bisa diatur sendiri lewat "Lainnya"
            Text("Setor cepat:", style = MaterialTheme.typography.labelLarge)
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf(1_000L, 2_000L, 5_000L, 10_000L).forEach { amount ->
                    FilterChip(
                        selected = false,
                        onClick = { onDeposit(amount) },
                        label = { Text(NumberFormat.getIntegerInstance().format(amount)) },
                    )
      
          }
                OutlinedButton(onClick = { showCustom = true }, modifier = Modifier.weight(1f)) {
                    Text("Lainnya", maxLines = 1)
                }
            }
        }
    }

    if (showCustom) {
        CustomAmountDialog(
            onDismiss = { showCustom = false },
            onConfirm = { onDeposit(it); showCustom = false },
        )
    }
}

/** Dialog nominal bebas: pilih cepat atau ketik sendiri. */
@Composable
private fun CustomAmountDialog(onDismiss: () -> Unit, onConfirm: (Long) -> Unit) {
    var text by remember { mutableStateOf("") }
    val parsed = text.replace("[^0-9]".toRegex(), "").toLongOrNull() ?: 0L

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Setor nominal sendiri") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Nominal (misal: 15000)") },
                    singleLine = true,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(3_000L, 20_000L, 50_000L).forEach { amount ->
                        FilterChip(
                            selected = false,
                            onClick = { onConfirm(amount) },
                            label = { Text(NumberFormat.getIntegerInstance().format(amount)) },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (parsed > 0) onConfirm(parsed) },
                enabled = parsed > 0,
            ) { Text("Setor") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } },
    )
}

/** Dialog buat/ubah target: nama, nominal target, tanggal deadline, foto. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
priv
ate fun GoalDialog(
    existing: SavingGoalEntity?,
    onDismiss: () -> Unit,
    onSave: (name: String, target: Long, deadline: Long, photo: Uri?) -> Unit,
) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var targetText by remember {
        mutableStateOf(existing?.targetAmount?.let { NumberFormat.getIntegerInstance().format(it) } ?: "")
    }
    val target = targetText.replace("[^0-9]".toRegex(), "").toLongOrNull() ?: 0L

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = existing?.deadline ?: Calendar.getInstance().apply {
            add(Calendar.MONTH, 3) // default: 3 bulan ke depan
        }.timeInMillis,
    )

    var photoUri by remember { mutableStateOf<Uri?>(null) }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        photoUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Menabung untuk sesuatu" else "Ubah Target") },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Untuk apa? (misal: Beli laptop)") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = targetText,
                    onValueChange = { targetText = it },
                    label = { Text("Target uang (misal: 5000000)") },
                    singleLine = true,
                )
                Text("Tanggal target:", style = MaterialTheme.typography.labelLarge)
                DatePicker(state = datePickerState)

                // Upload foto target
                if (photoUri != null) {
                    Row(verticalAlignment = Alignment.Cent
erVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Foto siap dipakai", Modifier.weight(1f))
                        IconButton(onClick = { photoUri = null }) {
                            Icon(Icons.Filled.Close, contentDescription = "Hapus foto")
                        }
                    }
                } else if (existing?.photoPath != null) {
                    Text("Foto lama tetap dipakai (pilih lagi untuk ganti)", style = MaterialTheme.typography.bodySmall)
                }
                OutlinedButton(onClick = { photoPicker.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (photoUri == null) "Upload Foto Target" else "Ganti Foto")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank() && target > 0) {
                        onSave(name, target, datePickerState.selectedDateMillis ?: 0L, photoUri)
                    }
                },
                enabled = name.isNotBlank() && target > 0,
            ) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Batal") } },
    )
}
