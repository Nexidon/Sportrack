package com.example.sportrack.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.sportrack.ui.progress.ProgressScreen
import com.example.sportrack.ui.settings.SettingsScreen
import com.example.sportrack.ui.settings.SettingsViewModel
import com.example.sportrack.ui.theme.SportrackTheme
import com.example.sportrack.ui.training.TrainingScreen

class MainActivity : ComponentActivity() {

    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // підписка на стан теми через collectAsStateWithLifecycle
            val uiState by settingsViewModel.uiState.collectAsStateWithLifecycle()

            SportrackTheme(darkTheme = uiState.isDarkTheme) {
                MainScreen(settingsViewModel)
            }
        }
    }
}

// --- навігаційні екрани ---
sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Progress : Screen("progress", "Прогрес", Icons.Filled.ShowChart)
    object Workouts : Screen("workouts", "Тренування", Icons.Filled.FitnessCenter)
    object Settings : Screen("settings", "Налаштування", Icons.Filled.Settings)
}

@Composable
fun MainScreen(settingsViewModel: SettingsViewModel) {
    val navController = rememberNavController()
    val items = listOf(Screen.Progress, Screen.Workouts, Screen.Settings)

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentRoute == screen.route,
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
            startDestination = Screen.Progress.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Progress.route) {
                ProgressScreen()
            }
            composable(Screen.Workouts.route) {
                TrainingScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen(settingsViewModel)
            }
        }
    }
}
