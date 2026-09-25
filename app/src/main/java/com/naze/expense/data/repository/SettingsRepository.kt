package com.naze.expense.data.repository

import com.naze.expense.data.preferences.SettingsDataStore
import com.naze.expense.data.preferences.UserSettings
import com.naze.expense.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

class SettingsRepository(private val dataStore: SettingsDataStore) {

    val settings: Flow<UserSettings> = dataStore.settings

    suspend fun setCurrency(code: String) = dataStore.setCurrency(code)

    suspend fun setTheme(mode: ThemeMode) = dataStore.setTheme(mode)
}
