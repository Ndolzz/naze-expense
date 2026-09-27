package com.naze.expense

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
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
import com.naze.expense.ui.budget.BudgetScreen
import com.naze.expense.ui.budget.BudgetViewModel
import com.naze.expense.ui.components.NazeBottomBar
import com.naze.expense.ui.components.NazeMoreSheet
import com.naze.expense.ui.dashboard.DashboardScreen
import com.naze.expense.ui.dashboard.DashboardViewModel
import com.naze.expense.ui.history.HistoryScreen
import com.naze.expense.ui.history.HistoryViewModel
import com.naze.expense.ui.navigation.Routes
import com.naze.expense.ui.navigation.moreDestinations
import com.naze.expense.ui.navigation.topLevelDestinations
import com.naze.expense.ui.navigation.topLevelRoutes
import com.naze.expense.ui.savings.SavingsScreen
import com.naze.expense.ui.savings.SavingsViewModel
import com.naze.expense.ui.settings.SettingsScreen
import com.naze.expense.ui.settings.SettingsViewModel
import com.naze.expense.ui.splash.NazeSplash
import com.naze.expense.ui.statistics.StatisticsScreen
import com.naze.expense.ui.statistics.StatisticsViewModel
import com.naze.expense.ui.theme.NazeExpenseTheme
import com.naze.expense.ui.transaction.AddEditTransactionScreen
import com.naze.expense.ui.transaction.TransactionViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as NazeExpenseApp).appContainer

        // Android 9 ke bawah: izin tulis storage publik untuk auto-backup.
        // Android 10+ tidak butuh permission (MediaStore).
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                1001,
            )
        }

        setContent {
            val settings by container.settingsRepository.settings
                .collectAsState(initial = UserSettings())
            var showSplash by remember { mutableStateOf(true) }

            NazeExpenseTheme(themeMode = settings.themeMode) {
                Crossfade(targetState = showSplash, label = "splash") { splash ->
                    if (splash) {
                        NazeSplash(onFinished = { showSplash = false })
                    } else {
                        NazeApp(container)
                    }
                }
            }
        }
    }

    // Auto-backup tiap kali app keluar ke background: data tersimpan di
    // Documents/NazeFinancialOS dan tetap ada walau aplikasi di-uninstall.
    override fun onPause() {
        super.onPause()
        val container = (application as NazeExpenseApp).appContainer
        lifecycleScope.launch(Dispatchers.IO) { container.autoBackup() }
    }

    @Composable
    private fun NazeApp(container: AppContainer) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            // Responsif: layar lebar (tablet / landscape / foldable) memakai
            // NavigationRail, layar HP memakai bottom navigation 4-item.
            if (maxWidth >= 720.dp) ExpandedApp(container) else CompactApp(container)
        }
    }

    /**
     * Arsitektur FAB tunggal:
     * FAB "tambah transaksi" HANYA dirender pada halaman Home. Halaman lain
     * (Menabung, Budget, dst.) memakai action khusus di dalam halamannya
     * masing-masing, sehingga dua tombol "+" tidak mungkin muncul bersamaan.
     */
    private fun NavHostController.navigateTopLevel(route: String) {
        navigate(route) {
            popUpTo(Routes.DASHBOARD) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    @Composable
    private fun CompactApp(container: AppContainer) {
        val navController: NavHostController = rememberNavController()
        val backStack by navController.currentBackStackEntryAsState()
        val currentRoute = backStack?.destination?.route

        val showBottomBar = currentRoute in topLevelRoutes
        // SATU-satunya FAB transaksi: hanya di Home.
        val showTransactionFab = currentRoute == Routes.DASHBOARD
        var showMoreSheet by remember { mutableStateOf(false) }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                if (showBottomBar) {
                    NazeBottomBar(
                        selectedRoute = currentRoute,
                        onDestinationClick = { navController.navigateTopLevel(it) },
                        onMoreClick = { showMoreSheet = true },
                    )
                }
            },
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                NavGraph(navController, container)
                if (showTransactionFab) {
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

        if (showMoreSheet) {
            NazeMoreSheet(
                selectedRoute = currentRoute,
                onDismiss = { showMoreSheet = false },
                onDestinationClick = { navController.navigateTopLevel(it) },
            )
        }
    }

    @Composable
    private fun ExpandedApp(container: AppContainer) {
        val navController: NavHostController = rememberNavController()
        val backStack by navController.currentBackStackEntryAsState()
        val currentRoute = backStack?.destination?.route

        val showRail = currentRoute in topLevelRoutes
        // Di layar lebar semua 6 destinasi langsung ada di rail (tanpa sheet),
        // tapi FAB transaksi tetap hanya muncul di Home.
        val showTransactionFab = currentRoute == Routes.DASHBOARD

        Row(Modifier.fillMaxSize()) {
            if (showRail) {
                NavigationRail(
                    header = {
                        if (showTransactionFab) {
                            FloatingActionButton(
                                onClick = { navController.navigate(Routes.addTransaction()) },
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = "Tambah transaksi")
                            }
                        }
                    },
                ) {
                    (topLevelDestinations + moreDestinations).forEach { item ->
                        NavigationRailItem(
                            icon = {
                                Icon(
                                    item.icon,
                                    contentDescription = item.label,
                                )
                            },
                            label = { Text(item.label) },
                            selected = currentRoute == item.route,
                            onClick = { navController.navigateTopLevel(item.route) },
                        )
                    }
                }
            }
            Box(Modifier.weight(1f).fillMaxHeight()) {
                NavGraph(navController, container)
            }
        }
    }

    @Composable
    private fun NavGraph(
        navController: NavHostController,
        container: AppContainer,
    ) {
        val viewModelFactory = viewModelFactory {
            initializer { DashboardViewModel(container) }
            initializer { HistoryViewModel(container) }
            initializer { BudgetViewModel(container) }
            initializer { StatisticsViewModel(container) }
            initializer { SettingsViewModel(container) }
            initializer { SavingsViewModel(container) }
        }

        NavHost(
            navController = navController,
            startDestination = Routes.DASHBOARD,
        ) {
            composable(Routes.DASHBOARD) {
                val vm: DashboardViewModel = viewModel(factory = viewModelFactory)
                DashboardScreen(
                    viewModel = vm,
                    onTransactionClick = { id -> navController.navigate(Routes.addTransaction(id)) },
                )
            }
            composable(Routes.HISTORY) {
                val vm: HistoryViewModel = viewModel(factory = viewModelFactory)
                HistoryScreen(
                    viewModel = vm,
                    onTransactionClick = { id -> navController.navigate(Routes.addTransaction(id)) },
                )
            }
            composable(Routes.SAVINGS) {
                val vm: SavingsViewModel = viewModel(factory = viewModelFactory)
                SavingsScreen(viewModel = vm)
            }
            composable(Routes.STATISTICS) {
                val vm: StatisticsViewModel = viewModel(factory = viewModelFactory)
                StatisticsScreen(viewModel = vm)
            }
            composable(Routes.BUDGET) {
                val vm: BudgetViewModel = viewModel(factory = viewModelFactory)
                BudgetScreen(viewModel = vm)
            }
            composable(Routes.SETTINGS) {
                val vm: SettingsViewModel = viewModel(factory = viewModelFactory)
                SettingsScreen(viewModel = vm)
            }
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
