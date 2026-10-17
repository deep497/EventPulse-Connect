package com.example.eventpulse.ui.organizer

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.eventpulse.ui.organizerViewModel
import com.example.eventpulse.ui.navigation.BottomNavigationBar
import com.example.eventpulse.ui.navigation.Screen
import com.example.eventpulse.ui.navigation.getOrganizerBottomNavItems
import kotlin.system.exitProcess

@Composable
fun OrganizerMainScreen(
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
                items = getOrganizerBottomNavItems()
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.OrgDashboard.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.OrgDashboard.route) {
                OrganizerDashboardScreen(
                    navController = parentNavController,
                    viewModel = organizerViewModel()
                )
            }
            
            composable(Screen.OrgEvents.route) {
                OrganizerEventsScreen(
                    navController = parentNavController,
                    viewModel = organizerViewModel()
                )
            }
            
            composable(Screen.OrgAttendees.route) {
                OrganizerAttendeesScreen(
                    navController = parentNavController,
                    viewModel = organizerViewModel()
                )
            }
            
            composable(Screen.OrgSettings.route) {
                OrganizerSettingsScreen(
                    navController = parentNavController,
                    viewModel = organizerViewModel()
                )
            }
        }
    }
}
