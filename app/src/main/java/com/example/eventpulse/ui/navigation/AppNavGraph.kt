package com.example.eventpulse.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.eventpulse.ui.admin.AdminMainScreen
import com.example.eventpulse.ui.auth.LoginScreen
import com.example.eventpulse.ui.auth.SignupScreen
import com.example.eventpulse.ui.authViewModel
import com.example.eventpulse.ui.userViewModel
import com.example.eventpulse.ui.organizerViewModel
import com.example.eventpulse.ui.checkoutViewModel
import com.example.eventpulse.ui.checkout.CheckoutScreen
import com.example.eventpulse.ui.organizer.CreateEditEventScreen
import com.example.eventpulse.ui.organizer.OrganizerMainScreen
import com.example.eventpulse.ui.organizer.QrScannerScreen
import com.example.eventpulse.ui.user.EventDetailScreen
import com.example.eventpulse.ui.user.UserMainScreen

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
                viewModel = authViewModel()
            )
        }
        
        composable(Screen.Signup.route) {
            SignupScreen(
                navController = navController,
                viewModel = authViewModel()
            )
        }

        // User main screen with bottom nav
        composable(Screen.UserExplore.route) {
            UserMainScreen(parentNavController = navController)
        }

        // User detail screens (without bottom nav)
        composable(
            route = Screen.EventDetail.route,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            EventDetailScreen(
                navController = navController,
                eventId = eventId,
                viewModel = userViewModel()
            )
        }

        composable(
            route = Screen.Checkout.route,
            arguments = listOf(
                navArgument("eventId") { type = NavType.StringType },
                navArgument("quantity") {
                    type = NavType.IntType
                    defaultValue = 1
                }
            )
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            val quantity = backStackEntry.arguments?.getInt("quantity") ?: 1
            CheckoutScreen(
                navController = navController,
                eventId = eventId,
                initialQuantity = quantity,
                viewModel = checkoutViewModel()
            )
        }

        // Organizer main screen with bottom nav
        composable(Screen.OrgDashboard.route) {
            OrganizerMainScreen(parentNavController = navController)
        }

        // Organizer detail screens (without bottom nav)
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
                viewModel = organizerViewModel()
            )
        }

        composable(Screen.QrScanner.route) {
            QrScannerScreen(
                navController = navController,
                viewModel = organizerViewModel()
            )
        }

        // Admin main screen with bottom nav
        composable(Screen.AdminModeration.route) {
            AdminMainScreen(parentNavController = navController)
        }
    }
}
