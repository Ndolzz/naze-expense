package com.naze.expense

import android.app.Application
import com.naze.expense.data.local.db.NazeDatabase
import com.naze.expense.data.preferences.SettingsDataStore
import com.naze.expense.data.repository.BudgetRepository
import com.naze.expense.data.repository.CategoryRepository
import com.naze.expense.data.repository.SettingsRepository
import com.naze.expense.data.repository.TransactionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Manual dependency container (tanpa Hilt di V1, ringan & jelas).
 */
class NazeExpenseApp : Application() {

    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        appContainer = AppContainer(this)
        appContainer.seedDefaults()
    }
}

class AppContainer(context: android.content.Context) {
    val database: NazeDatabase = NazeDatabase.get(context)
    val transactionRepository = TransactionRepository(database.transactionDao(), database.categoryDao())
    val categoryRepository = CategoryRepository(database.categoryDao())
    val budgetRepository = BudgetRepository(database.budgetDao())
    val settingsRepository = SettingsRepository(SettingsDataStore(context))

    private val seedScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun seedDefaults() {
        seedScope.launch {
            categoryRepository.seedDefaultsIfEmpty()
        }
    }
}
