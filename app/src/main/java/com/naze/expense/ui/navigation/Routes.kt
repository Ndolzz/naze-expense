package com.naze.expense.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/** Route navigation (Navigation Compose). */
object Routes {
    const val DASHBOARD = "dashboard"
    const val HISTORY = "history"
    const val STATISTICS = "statistics"
    const val BUDGET = "budget"
    const val SAVINGS = "savings"
    const val SETTINGS = "settings"
    const val ADD_TRANSACTION = "add_transaction?transactionId={transactionId}"
    const val MANAGE_CATEGORIES = "manage_categories"

    fun addTransaction(editId: Long? = null) =
        "add_transaction?transactionId=" + (editId ?: -1L)
}

/**
 * Destinasi bottom navigation — SELALU 4 item:
 * Home, Riwayat, Statistik, dan "Lainnya" (bottom sheet).
 * Menabung / Budget / Setelan hidup di dalam sheet "Lainnya",
 * jadi 6 fitur tetap tersedia tanpa menjejalkan semuanya ke navbar.
 */
data class TopLevelDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

val topLevelDestinations = listOf(
    TopLevelDestination(Routes.DASHBOARD, "Home", Icons.Filled.Home),
    TopLevelDestination(Routes.HISTORY, "Riwayat", Icons.Filled.ReceiptLong),
    TopLevelDestination(Routes.STATISTICS, "Statistik", Icons.Filled.BarChart),
)

/** Item menu di dalam bottom sheet "Lainnya". */
data class MoreDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val description: String,
)

val moreDestinations = listOf(
    MoreDestination(Routes.SAVINGS, "Menabung", Icons.Filled.Savings, "Target tabungan & setor cepat"),
    MoreDestination(Routes.BUDGET, "Budget", Icons.Filled.AccountBalanceWallet, "Batas belanja per kategori"),
    MoreDestination(Routes.SETTINGS, "Setelan", Icons.Filled.Settings, "Kategori, tema, backup data"),
)

/** Semua rute top-level: bottom bar / rail tetap terlihat di rute ini. */
val topLevelRoutes: List<String> =
    topLevelDestinations.map { it.route } + moreDestinations.map { it.route }
