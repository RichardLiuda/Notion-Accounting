package com.RichardLiu.notionaccounting

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.RichardLiu.notionaccounting.ui.AccountingScreen
import com.RichardLiu.notionaccounting.ui.AddTransactionScreen
import com.RichardLiu.notionaccounting.ui.SettingsScreen
import com.RichardLiu.notionaccounting.ui.StatisticsScreen
import com.RichardLiu.notionaccounting.ui.theme.NotionAccountingTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NotionAccountingTheme {
                val navController = rememberNavController()
                val items = listOf(
                    Screen.Add,
                    Screen.Statistics,
                    Screen.Transactions,
                    Screen.Settings
                )
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    when (currentRoute) {
                                        Screen.Add.route -> "添加交易"
                                        Screen.Statistics.route -> "统计"
                                        Screen.Transactions.route -> "交易记录"
                                        Screen.Settings.route -> "设置"
                                        else -> "Notion记账"
                                    },
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                titleContentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    },
                    bottomBar = {
                        NavigationBar {
                            val currentDestination = navBackStackEntry?.destination
                            items.forEach { screen ->
                                NavigationBarItem(
                                    icon = { Icon(screen.icon, contentDescription = null) },
                                    label = { Text(screen.label) },
                                    selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
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
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Add.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.Add.route) { AddTransactionScreen() }
                        composable(Screen.Statistics.route) { StatisticsScreen() }
                        composable(Screen.Transactions.route) { AccountingScreen() }
                        composable(Screen.Settings.route) { SettingsScreen() }
                    }
                }
            }
        }
    }
}

sealed class Screen(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    object Add : Screen("add", "添加", Icons.Default.Add)
    object Statistics : Screen("statistics", "统计", Icons.Default.Info)
    object Transactions : Screen("transactions", "记录", Icons.Default.List)
    object Settings : Screen("settings", "设置", Icons.Default.Settings)
} 