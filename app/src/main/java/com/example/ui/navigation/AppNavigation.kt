package com.example.ui.navigation

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.R
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.RecordFormScreen
import com.example.ui.screens.RecordsHistoryScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SetupScreen
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.theme.ForestGreenPrimaryContainer
import com.example.ui.viewmodel.DailyRecordFormViewModel
import com.example.ui.viewmodel.FarmViewModel
import com.example.ui.viewmodel.ReportsViewModel
import com.example.util.BackupManager
import com.example.util.DateUtils
import java.util.Locale

sealed class Screen(
    val route: String,
    val titleRes: Int? = null,
    val selectedIcon: ImageVector? = null,
    val unselectedIcon: ImageVector? = null
) {
    object Setup : Screen("setup")
    object Home : Screen("home", R.string.nav_home, Icons.Filled.Home, Icons.Outlined.Home)
    object Records : Screen("records", R.string.nav_records, Icons.Filled.History, Icons.Outlined.History)
    object Reports : Screen("reports", R.string.nav_reports, Icons.Filled.Analytics, Icons.Outlined.Analytics)
    object Settings : Screen("settings", R.string.nav_settings, Icons.Filled.Settings, Icons.Outlined.Settings)
    object RecordForm : Screen("record_form?recordId={recordId}") {
        fun createRoute(recordId: Long? = null) = if (recordId != null) "record_form?recordId=$recordId" else "record_form"
    }
}

@Composable
fun AppNavigation(
    farmViewModel: FarmViewModel = viewModel(),
    navController: NavHostController = rememberNavController()
) {
    val farm by farmViewModel.farm.collectAsStateWithLifecycle()
    val isSetupCompleted by farmViewModel.isSetupCompleted.collectAsStateWithLifecycle()
    val language by farmViewModel.language.collectAsStateWithLifecycle()
    val stats by farmViewModel.dashboardStats.collectAsStateWithLifecycle()
    val allRecords by farmViewModel.allRecords.collectAsStateWithLifecycle()

    val context = LocalContext.current

    // Apply active locale dynamically for instantaneous bilingual UI rendering
    val locale = Locale(language)
    val localizedConfig = Configuration(LocalConfiguration.current).apply {
        setLocale(locale)
        setLayoutDirection(locale)
    }

    CompositionLocalProvider(
        LocalConfiguration provides localizedConfig
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        val bottomNavItems = listOf(
            Screen.Home,
            Screen.Records,
            Screen.Reports,
            Screen.Settings
        )

        val showBottomBar = currentRoute in bottomNavItems.map { it.route }

        val startDestination = if (isSetupCompleted || farm != null) {
            Screen.Home.route
        } else {
            Screen.Setup.route
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp
                    ) {
                        bottomNavItems.forEach { item ->
                            val selected = currentRoute == item.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon!! else item.unselectedIcon!!,
                                        contentDescription = item.titleRes?.let { stringResource(it) }
                                    )
                                },
                                label = {
                                    item.titleRes?.let {
                                        Text(
                                            text = stringResource(it),
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = ForestGreenPrimary,
                                    selectedTextColor = ForestGreenPrimary,
                                    indicatorColor = ForestGreenPrimaryContainer
                                ),
                                modifier = Modifier.testTag("nav_item_${item.route}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.padding(innerPadding)
            ) {
                // Setup Destination
                composable(Screen.Setup.route) {
                    SetupScreen(
                        onSetupComplete = { name, adults, chicks, date, lang ->
                            farmViewModel.setupFarm(name, adults, chicks, date, lang) {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.Setup.route) { inclusive = true }
                                }
                            }
                        }
                    )
                }

                // Home Destination
                composable(Screen.Home.route) {
                    HomeScreen(
                        farm = farm,
                        stats = stats,
                        onNavigateToAddRecord = {
                            navController.navigate(Screen.RecordForm.createRoute())
                        },
                        onNavigateToRecords = {
                            navController.navigate(Screen.Records.route)
                        },
                        onNavigateToReports = {
                            navController.navigate(Screen.Reports.route)
                        },
                        onNavigateToSettings = {
                            navController.navigate(Screen.Settings.route)
                        }
                    )
                }

                // Records History Destination
                composable(Screen.Records.route) {
                    RecordsHistoryScreen(
                        records = allRecords,
                        onEditRecord = { recordId ->
                            navController.navigate(Screen.RecordForm.createRoute(recordId))
                        },
                        onDeleteRecord = { recordId ->
                            farmViewModel.deleteRecord(recordId)
                        }
                    )
                }

                // Reports Destination
                composable(Screen.Reports.route) {
                    val reportsViewModel: ReportsViewModel = viewModel()
                    ReportsScreen(
                        viewModel = reportsViewModel,
                        farm = farm,
                        onExportReportCsv = {
                            val csv = BackupManager.generateReportsCsv(farm, allRecords)
                            BackupManager.shareContent(
                                context,
                                "poultry_farm_report_${DateUtils.getCurrentDateString()}.csv",
                                "text/csv",
                                csv
                            )
                        }
                    )
                }

                // Settings Destination
                composable(Screen.Settings.route) {
                    SettingsScreen(
                        viewModel = farmViewModel,
                        allRecords = allRecords,
                        onResetCompleted = {
                            navController.navigate(Screen.Setup.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }

                // Daily Record Form (Add or Edit)
                composable(
                    route = "record_form?recordId={recordId}",
                    arguments = listOf(
                        navArgument("recordId") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        }
                    )
                ) { backStackEntry ->
                    val recordIdStr = backStackEntry.arguments?.getString("recordId")
                    val recordId = recordIdStr?.toLongOrNull()
                    val formViewModel: DailyRecordFormViewModel = viewModel()

                    RecordFormScreen(
                        viewModel = formViewModel,
                        recordId = recordId,
                        onNavigateBack = { navController.popBackStack() },
                        onSaveSuccess = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
