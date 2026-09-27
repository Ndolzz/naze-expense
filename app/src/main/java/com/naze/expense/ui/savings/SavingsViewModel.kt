package com.naze.expense.ui.savings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naze.expense.AppContainer
import com.naze.expense.data.local.entity.SavingDepositEntity
import com.naze.expense.data.local.entity.SavingGoalEntity
import com.naze.expense.data.preferences.UserSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/** Model UI untuk satu target menabung. */
data class SavingGoalUi(
    val goal: SavingGoalEntity,
    val saved: Long,
    val daysLeft: Long,
    val perDay: Long,       // kalkulator: sisa / hari
    val perWeek: Long,
    val perMonth: Long,
    val progress: Float,
    val done: Boolean,
)

data class SavingsState(
    val settings: UserSettings = UserSettings(),
    val goals: List<SavingGoalUi> = emptyList(),
    val quote: String = "",
    val snackbar: String? = null,
)

class SavingsViewModel(private val container: AppContainer) : ViewModel() {

    private val repo = container.savingGoalRepository
    private val _snackbar = MutableStateFlow<String?>(null)

    val state: StateFlow<SavingsState> = combine(
        container.settingsRepository.settings,
        repo.observeAll(),
        _snackbar,
    ) { settings, goals, snackbar ->
        val now = System.currentTimeMillis()
        SavingsState(
            settings = settings,
            goals = goals.map { g ->
                val saved = g.saved
                val remain = g.goal.targetAmount - saved
                val daysLeft = ((g.goal.deadline - now) / 86_400_000L).coerceAtLeast(0L) + 1
                SavingGoalUi(
                    goal = g.goal,
                    saved = saved,
                    daysLeft = daysLeft,
                    perDay = if (remain <= 0 || daysLeft <= 0) 0 else (remain + daysLeft - 1) / daysLeft,
                    perWeek = if (remain <= 0) 0 else (remain + ((daysLeft + 6) / 7).coerceAtLeast(1) - 1) / ((daysLeft + 6) / 7).coerceAtLeast(1),
                    perMonth = if (remain <= 0 || daysLeft <= 0) 0
                        else (remain + ((daysLeft + 29) / 30).coerceAtLeast(1) - 1) / ((daysLeft + 29) / 30).coerceAtLeast(1),
                    progress = if (g.goal.targetAmount <= 0) 0f
                        else (saved.toFloat() / g.goal.targetAmount).coerceIn(0f, 1f),
                    done = remain <= 0,
                )
            },
            quote = com.naze.expense.domain.model.MotivationQuotes.today(),
            snackbar = snackbar,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SavingsState())

    fun consumeSnackbar() { _snackbar.value = null }

    fun addGoal(name: String, targetAmount: Long, deadline: Long, photoUri: Uri?, context: Context) {
        if (name.isBlank() || targetAmount <= 0) {
            _snackbar.value = "Nama dan target harus diisi"
            return
        }
        viewModelScope.launch {
            val photoPath = photoUri?.let { copyPhoto(context, it) }
            repo.add(SavingGoalEntity(name = name.trim(), targetAmount = targetAmount, deadline = deadline, photoPath = photoPath))
            _snackbar.value = "Target menabung dibuat. Semangat!"
        }
    }

    fun updateGoal(goal: SavingGoalEntity, name: String, targetAmount: Long, deadline: Long, photoUri: Uri?, context: Context) {
        viewModelScope.launch {
            val newPhoto = photoUri?.let { copyPhoto(context, it) }
            repo.update(goal.copy(
                name = name.trim(),
                targetAmount = targetAmount,
                deadline = deadline,
                photoPath = newPhoto ?: goal.photoPath,
            ))
            _snackbar.value = "Target diperbarui"
        }
    }

    fun deposit(goal: SavingGoalEntity, amount: Long) {
        if (amount <= 0) {
            _snackbar.value = "Nominal tidak valid"
            return
        }
        viewModelScope.launch {
            repo.deposit(SavingDepositEntity(goalId = goal.id, amount = amount))
            _snackbar.value = "Berhasil menabung " + amount
        }
    }

    fun deleteGoal(goal: SavingGoalEntity) {
        viewModelScope.launch {
            goal.photoPath?.let { File(it).delete() }
            repo.delete(goal)
            _snackbar.value = "Target dihapus"
        }
    }

    /** Salin foto yang dipilih ke internal storage supaya tidak hilang walau URI galeri kedaluwarsa. */
    private suspend fun copyPhoto(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        runCatching {
            val dir = File(context.filesDir, "goal_photos").apply { mkdirs() }
            val file = File(dir, UUID.randomUUID().toString() + ".jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { input.copyTo(it) }
            } ?: return@runCatching null
            file.absolutePath
        }.getOrNull()
    }
}
