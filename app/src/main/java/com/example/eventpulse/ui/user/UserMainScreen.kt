package com.example.eventpulse.ui.user

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.eventpulse.ui.userViewModel
import com.example.eventpulse.ui.navigation.BottomNavigationBar
import com.example.eventpulse.ui.navigation.Screen
import com.example.eventpulse.ui.navigation.getUserBottomNavItems
import kotlin.system.exitProcess

@Composable
fun UserMainScreen(
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
                items = getUserBottomNavItems()
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.UserExplore.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.UserExplore.route) {
                UserHomeScreen(
                    navController = parentNavController,
                    viewModel = userViewModel()
                )
            }
            
            composable(Screen.UserFavorites.route) {
                UserFavoritesScreen(
                    navController = parentNavController,
                    viewModel = userViewModel()
                )
            }
            
            composable(Screen.UserTickets.route) {
                MyTicketsScreen(
                    navController = parentNavController,
                    viewModel = userViewModel()
                )
            }
            
            composable(Screen.UserProfile.route) {
                UserProfileScreen(
                    navController = parentNavController,
                    viewModel = userViewModel()
                )
            }
        }
    }
}
