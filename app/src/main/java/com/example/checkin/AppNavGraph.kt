package com.example.checkin

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.checkin.ui.home.HomeScreen
import com.example.checkin.ui.project.ProjectManagementScreen
import com.example.checkin.ui.settings.SettingsScreen
import com.example.checkin.ui.stats.StatsScreen

object Routes {
    const val HOME = "home"
    const val STATS = "stats"
    const val SETTINGS = "settings"
    const val PROJECT_MANAGEMENT = "project_management"
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        modifier = modifier
    ) {
        composable(Routes.HOME) {
            HomeScreen()
        }
        composable(Routes.STATS) {
            StatsScreen()
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onNavigateToProjectManagement = {
                    navController.navigate(Routes.PROJECT_MANAGEMENT)
                }
            )
        }
        composable(Routes.PROJECT_MANAGEMENT) {
            ProjectManagementScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
