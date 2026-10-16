package com.example.eventpulse.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.eventpulse.ui.admin.AdminDashboardScreen
import com.example.eventpulse.ui.auth.LoginScreen
import com.example.eventpulse.ui.auth.SignupScreen
import com.example.eventpulse.ui.organizer.CreateEditEventScreen
import com.example.eventpulse.ui.organizer.OrganizerDashboardScreen
import com.example.eventpulse.ui.organizer.QrScannerScreen
import com.example.eventpulse.ui.user.EventDetailScreen
import com.example.eventpulse.ui.user.MyTicketsScreen
import com.example.eventpulse.ui.user.UserHomeScreen
import com.example.eventpulse.ui.eventPulseViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth screens
        composable(Screen.Login.route) {
            LoginScreen(
                navController = navController,
                viewModel = eventPulseViewModel()
            )
        }
        
        composable(Screen.Signup.route) {
            SignupScreen(
                navController = navController,
                viewModel = eventPulseViewModel()
            )
        }

        // User screens
        composable(Screen.UserHome.route) {
            UserHomeScreen(
                navController = navController,
                viewModel = eventPulseViewModel()
            )
        }

        composable(
            route = Screen.EventDetail.route,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            EventDetailScreen(
                navController = navController,
                eventId = eventId,
                viewModel = eventPulseViewModel()
            )
        }

        composable(Screen.MyTickets.route) {
            MyTicketsScreen(
                navController = navController,
                viewModel = eventPulseViewModel()
            )
        }

        // Organizer screens
        composable(Screen.OrganizerDashboard.route) {
            OrganizerDashboardScreen(
                navController = navController,
                viewModel = eventPulseViewModel()
            )
        }

        composable(
            route = Screen.CreateEditEvent.route,
            arguments = listOf(navArgument("eventId") { 
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId")
            CreateEditEventScreen(
                navController = navController,
                eventId = eventId,
                viewModel = eventPulseViewModel()
            )
        }

        composable(Screen.QrScanner.route) {
            QrScannerScreen(
                navController = navController,
                viewModel = eventPulseViewModel()
            )
        }

        // Admin screens
        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(
                navController = navController,
                viewModel = eventPulseViewModel()
            )
        }
    }
}
