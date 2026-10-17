package com.example.eventpulse.ui.admin

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.eventpulse.ui.adminViewModel
import com.example.eventpulse.ui.navigation.BottomNavigationBar
import com.example.eventpulse.ui.navigation.Screen
import com.example.eventpulse.ui.navigation.getAdminBottomNavItems
import kotlin.system.exitProcess

@Composable
fun AdminMainScreen(
    parentNavController: NavHostController
) {
    val navController = rememberNavController()
    
    // Handle back press from root screens - exit app
    BackHandler {
        exitProcess(0)
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                items = getAdminBottomNavItems()
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.AdminModeration.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.AdminModeration.route) {
                AdminDashboardScreen(
                    navController = parentNavController,
                    viewModel = adminViewModel()
                )
            }
            
            composable(Screen.AdminUsers.route) {
                AdminUsersScreen(
                    navController = parentNavController,
                    viewModel = adminViewModel()
                )
            }
            
            composable(Screen.AdminAnalytics.route) {
                AdminAnalyticsScreen(
                    navController = parentNavController,
                    viewModel = adminViewModel()
                )
            }
        }
    }
}
