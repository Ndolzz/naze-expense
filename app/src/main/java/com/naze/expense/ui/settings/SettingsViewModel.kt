package com.naze.expense.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naze.expense.AppContainer
import com.naze.expense.data.backup.BackupManager
import com.naze.expense.data.preferences.UserSettings
import com.naze.expense.domain.model.Category
import com.naze.expense.domain.model.ThemeMode
import com.naze.expense.domain.model.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsState(
    val settings: UserSettings = UserSettings(),
    val categories: List<Category> = emptyList(),
    val snackbar: String? = null,
)

class SettingsViewModel(private val container: AppContainer) : ViewModel() {

    private val settingsRepo = container.settingsRepository
    private val catRepo = container.categoryRepository
    private val backupManager = BackupManager(container)

    private val _snackbar = MutableStateFlow<String?>(null)

    val state: StateFlow<SettingsState> = combine(
        settingsRepo.settings,
        catRepo.observeAll(),
        _snackbar,
    ) { settings, categories, snackbar ->
        SettingsState(settings, categories, snackbar)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsState())

    fun setCurrency(code: String) {
        viewModelScope.launch {
            settingsRepo.setCurrency(code)
            _snackbar.value = "Mata uang diubah ke " + code
        }
    }

    fun setTheme(mode: ThemeMode) {
        viewModelScope.launch { settingsRepo.setTheme(mode) }
    }

    fun notify(message: String) { _snackbar.value = message }

    fun consumeSnackbar() { _snackbar.value = null }

    /** Tambah atau ubah kategori custom. */
    fun saveCategory(existing: Category?, name: String, type: TransactionType, colorHex: String) {
        if (name.isBlank()) {
            _snackbar.value = "Nama kategori tidak boleh kosong"
            return
        }
        viewModelScope.launch {
            val category = Category(
                id = existing?.id ?: 0L,
                name = name.trim(),
                icon = existing?.icon ?: "category",
                colorHex = existing?.colorHex ?: colorHex,
                isDefault = existing?.isDefault ?: false,
                type = type,
            )
            if (existing == null) catRepo.add(category) else catRepo.update(category)
            _snackbar.value = "Kategori disimpan"
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            val ok = catRepo.delete(category)
            _snackbar.value = if (ok) "Kategori dihapus"
            else "Kategori masih dipakai transaksi dan tidak bisa dihapus"
        }
    }

    suspend fun exportJson(): String = backupManager.export()

    fun importJson(text: String) {
        viewModelScope.launch {
            runCatching { backupManager.import(text) }
                .onSuccess { _snackbar.value = "Restore berhasil" }
                .onFailure { _snackbar.value = "Restore gagal: file tidak valid" }
        }
    }
}
