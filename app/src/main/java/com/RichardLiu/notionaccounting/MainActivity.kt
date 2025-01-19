package com.RichardLiu.notionaccounting

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.RichardLiu.notionaccounting.ui.AccountingScreen
import com.RichardLiu.notionaccounting.ui.AddTransactionScreen
import com.RichardLiu.notionaccounting.ui.SettingsScreen
import com.RichardLiu.notionaccounting.ui.StatisticsScreen
import com.RichardLiu.notionaccounting.ui.theme.NotionAccountingTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    sealed class Screen(val route: String, val icon: ImageVector, val label: String) {
        object AddTransaction : Screen("add_transaction", Icons.Default.Add, "添加")
        object Statistics : Screen("statistics", Icons.Default.Info, "统计")
        object Accounting : Screen("accounting", Icons.Default.List, "记录")
        object Settings : Screen("settings", Icons.Default.Settings, "设置")

        companion object {
            val items = listOf(AddTransaction, Statistics, Accounting, Settings)
            
            fun fromRoute(route: String?): Screen? = when(route) {
                AddTransaction.route -> AddTransaction
                Statistics.route -> Statistics
                Accounting.route -> Accounting
                Settings.route -> Settings
                else -> null
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NotionAccountingTheme {
                val navController = rememberNavController()
                val currentBackStack by navController.currentBackStackEntryAsState()
                val currentScreen = Screen.fromRoute(currentBackStack?.destination?.route)

                Scaffold(
                    topBar = {
                        LargeTopAppBar(
                            title = { Text(text = when(currentScreen) {
                                Screen.AddTransaction -> "添加交易"
                                Screen.Statistics -> "统计"
                                Screen.Accounting -> "交易记录"
                                Screen.Settings -> "设置"
                                null -> "Notion记账"
                            }) }
                        )
                    },
                    bottomBar = {
                        NavigationBar {
                            Screen.items.forEach { screen ->
                                NavigationBarItem(
                                    icon = { Icon(screen.icon, contentDescription = null) },
                                    label = { Text(screen.label) },
                                    selected = currentScreen == screen,
                                    onClick = {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                ) { paddingValues ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Accounting.route,
                        modifier = Modifier.padding(paddingValues)
                    ) {
                        composable(Screen.AddTransaction.route) {
                            AddTransactionScreen()
                        }
                        composable(Screen.Statistics.route) {
                            StatisticsScreen()
                        }
                        composable(Screen.Accounting.route) {
                            AccountingScreen()
                        }
                        composable(Screen.Settings.route) {
                            SettingsScreen()
                        }
                    }
                }
            }
        }
    }
} 