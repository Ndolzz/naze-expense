package com.naze.expense

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.naze.expense.data.preferences.UserSettings
import com.naze.expense.ui.components.PlaceholderScreen
import com.naze.expense.ui.dashboard.DashboardScreen
import com.naze.expense.ui.dashboard.DashboardViewModel
import com.naze.expense.ui.navigation.Routes
import com.naze.expense.ui.navigation.bottomNavItems
import com.naze.expense.ui.theme.NazeExpenseTheme
import com.naze.expense.ui.transaction.AddEditTransactionScreen
import com.naze.expense.ui.transaction.TransactionViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as NazeExpenseApp).appContainer

        setContent {
            val settings by container.settingsRepository.settings
                .collectAsState(initial = UserSettings())
            NazeExpenseTheme(themeMode = settings.themeMode) {
                NazeApp(container)
            }
        }
    }

    @Composable
    private fun NazeApp(container: AppContainer) {
        val navController: NavHostController = rememberNavController()
        val backStack by navController.currentBackStackEntryAsState()
        val currentRoute = backStack?.destination?.route

        val showBottomBar = currentRoute in bottomNavItems.map { it.route }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar {
                        bottomNavItems.forEach { item ->
                            NavigationBarItem(
                                icon = { Icon(item.icon, contentDescription = item.label) },
                                label = { Text(item.label) },
                                selected = currentRoute == item.route,
                                onClick = {
                                    navController.navigate(item.route) {
                                        popUpTo(Routes.DASHBOARD) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                            )
                        }
                    }
                }
            },
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                NavGraph(navController, container)
                if (showBottomBar) {
                    FloatingActionButton(
                        onClick = { navController.navigate(Routes.addTransaction()) },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp),
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "Tambah transaksi")
                    }
                }
            }
        }
    }

    @Composable
    private fun NavGraph(
        navController: NavHostController,        container: AppContainer,
    ) {
        val dashboardFactory = viewModelFactory {
            initializer { DashboardViewModel(container) }
        }

        NavHost(
            navController = navController,
            startDestination = Routes.DASHBOARD,
        ) {
            composable(Routes.DASHBOARD) {
                val vm: DashboardViewModel = viewModel(factory = dashboardFactory)
                DashboardScreen(
                    viewModel = vm,
                    onTransactionClick = { id -> navController.navigate(Routes.addTransaction(id)) },
                )
            }
            composable(Routes.HISTORY) { PlaceholderScreen("Riwayat") }
            composable(Routes.STATISTICS) { PlaceholderScreen("Statistik") }
            composable(Routes.BUDGET) { PlaceholderScreen("Budget") }
            composable(Routes.SETTINGS) { PlaceholderScreen("Setelan") }
            composable(
                route = Routes.ADD_TRANSACTION,
                arguments = listOf(navArgument("transactionId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }),
            ) { entry ->
                val editId = entry.arguments?.getLong("transactionId")?.takeIf { it > 0 }
                val vm: TransactionViewModel = viewModel(
                    factory = viewModelFactory {
                        initializer { TransactionViewModel(container, editId) }
                    }
                )
                AddEditTransactionScreen(
                    viewModel = vm,
                    onDone = { navController.popBackStack() },
                )
            }
        }
    }
}
