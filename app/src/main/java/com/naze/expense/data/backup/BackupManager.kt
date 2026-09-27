package com.naze.expense.data.backup

import com.naze.expense.AppContainer
import com.naze.expense.data.local.entity.BudgetEntity
import com.naze.expense.data.local.entity.CategoryEntity
import com.naze.expense.data.local.entity.SavingDepositEntity
import com.naze.expense.data.local.entity.SavingGoalEntity
import com.naze.expense.data.local.entity.TransactionEntity
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class CategoryDto(
    val id: Long,
    val name: String,
    val icon: String,
    val colorHex: String,
    val isDefault: Boolean,
    val type: String,
)

@Serializable
data class BudgetDto(val categoryId: Long, val monthlyLimit: Long)

@Serializable
data class TransactionDto(
    val id: Long,
    val amount: Long,
    val type: String,
    val categoryId: Long,
    val note: String? = null,
    val date: Long,
)

@Serializable
data class SavingGoalDto(
    val id: Long,
    val name: String,
    val targetAmount: Long,
    val deadline: Long,
    val createdAt: Long,
)

@Serializable
data class SavingDepositDto(
    val id: Long,
    val goalId: Long,
    val amount: Long,
    val date: Long,
)

@Serializable
data class BackupData(
    val version: Int = 2,
    val exportedAt: Long,
    val categories: List<CategoryDto>,
    val budgets: List<BudgetDto>,
    val transactions: List<TransactionDto>,
    // v2: data tabungan ikut dibackup (default kosong untuk file backup v1 lama)
    val savingGoals: List<SavingGoalDto> = emptyList(),
    val savingDeposits: List<SavingDepositDto> = emptyList(),
)

/**
 * Backup & restore 100% lokal: ekspor/impor seluruh data (kategori, budget,
 * transaksi, dan target menabung) ke file JSON via Storage Access Framework.
 * Tanpa jaringan. (Foto target tidak ikut — hanya path internal.)
 */
class BackupManager(private val container: AppContainer) {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }

    suspend fun export(): String {
        val db = container.database
        val savingDao = db.savingGoalDao()
        return json.encodeToString(
            BackupData(
                exportedAt = System.currentTimeMillis(),
                categories = db.categoryDao().getAll().map {
                    CategoryDto(it.id, it.name, it.icon, it.colorHex, it.isDefault, it.type)
                },
                budgets = db.budgetDao().getAll().map {
                    BudgetDto(it.categoryId, it.monthlyLimit)
                },
                transactions = db.transactionDao().getAll().map {
                    TransactionDto(it.id, it.amount, it.type, it.categoryId, it.note, it.date)
                },
                savingGoals = savingDao.getAllGoals().map {
                    SavingGoalDto(it.id, it.name, it.targetAmount, it.deadline, it.createdAt)
                },
                savingDeposits = savingDao.getAllDeposits().map {
                    SavingDepositDto(it.id, it.goalId, it.amount, it.date)
                },
            )
        )
    }

    suspend fun import(text: String) {
        val data = json.decodeFromString<BackupData>(text)
        val db = container.database
        val savingDao = db.savingGoalDao()
        // Ganti transaksi lama; kategori di-insert dengan id asli (IGNORE jika sudah ada)
        db.transactionDao().deleteAll()
        db.categoryDao().insertAll(
            data.categories.map {
                CategoryEntity(it.id, it.name, it.icon, it.colorHex, it.isDefault, it.type)
            }
        )
        data.budgets.forEach {
            db.budgetDao().upsert(
                BudgetEntity(categoryId = it.categoryId, monthlyLimit = it.monthlyLimit)
            )
        }
        db.transactionDao().insertAll(
            data.transactions.map {
                TransactionEntity(it.id, it.amount, it.type, it.categoryId, it.note, it.date)
            }
        )
        // v2: pulihkan tabungan
        savingDao.deleteAllDeposits()
        savingDao.deleteAllGoals()
        data.savingGoals.forEach {
            savingDao.insertGoal(SavingGoalEntity(it.id, it.name, it.targetAmount, it.deadline, null, it.createdAt))
        }
        data.savingDeposits.forEach {
            savingDao.insertDeposit(SavingDepositEntity(it.id, it.goalId, it.amount, it.date))
        }
    }
}
