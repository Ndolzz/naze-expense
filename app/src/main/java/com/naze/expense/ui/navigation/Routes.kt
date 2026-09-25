package com.naze.expense.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.ui.graphics.vector.ImageVector

/** Route navigation (Navigation Compose). */
object Routes {
    const val DASHBOARD = "dashboard"
    const val HISTORY = "history"
    const val STATISTICS = "statistics"
    const val BUDGET = "budget"
    const val SETTINGS = "settings"
    const val ADD_TRANSACTION = "add_transaction?transactionId={transactionId}"
    const val MANAGE_CATEGORIES = "manage_categories"

    fun addTransaction(editId: Long? = null) =
        "add_transaction?transactionId=" + (editId ?: -1L)
}

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

val bottomNavItems = listOf(
    BottomNavItem(Routes.DASHBOARD, "Home", Icons.Filled.Home),
    BottomNavItem(Routes.HISTORY, "Riwayat", Icons.Filled.ReceiptLong),
    BottomNavItem(Routes.STATISTICS, "Statistik", Icons.Filled.BarChart),
    BottomNavItem(Routes.BUDGET, "Budget", Icons.Filled.Savings),
    BottomNavItem(Routes.SETTINGS, "Setelan", Icons.Filled.Settings),
)
