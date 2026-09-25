package com.naze.expense.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.naze.expense.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

data class UserSettings(
    val currencyCode: String = "IDR",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
)

class SettingsDataStore(private val context: Context) {

    private val KEY_CURRENCY = stringPreferencesKey("currency_code")
    private val KEY_THEME = stringPreferencesKey("theme_mode")

    val settings: Flow<UserSettings> = context.dataStore.data.map { prefs ->
        UserSettings(
            currencyCode = prefs[KEY_CURRENCY] ?: "IDR",
            themeMode = prefs[KEY_THEME]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
                ?: ThemeMode.SYSTEM,
        )
    }

    suspend fun setCurrency(code: String) {
        context.dataStore.edit { it[KEY_CURRENCY] = code }
    }

    suspend fun setTheme(mode: ThemeMode) {
        context.dataStore.edit { it[KEY_THEME] = mode.name }
    }
}
