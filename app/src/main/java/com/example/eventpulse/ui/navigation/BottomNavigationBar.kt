package com.example.eventpulse.ui.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavigationBar(
    navController: NavController,
    items: List<BottomNavItem>
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { 
                    Icon(
                        painter = painterResource(id = getIconForRoute(item.icon)),
                        contentDescription = item.title
                    )
                },
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        // Pop up to the start destination and save state
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                }
            )
        }
    }
}

private fun getIconForRoute(iconName: String): Int {
    return when (iconName) {
        // User icons
        "explore" -> android.R.drawable.ic_menu_search
        "favorite" -> android.R.drawable.btn_star_big_on
        "confirmation_number" -> android.R.drawable.ic_menu_today
        "person" -> android.R.drawable.ic_menu_myplaces
        
        // Organizer icons
        "dashboard" -> android.R.drawable.ic_menu_view
        "event" -> android.R.drawable.ic_menu_today
        "people" -> android.R.drawable.ic_menu_myplaces
        "settings" -> android.R.drawable.ic_menu_preferences
        
        // Admin icons
        "gavel" -> android.R.drawable.ic_menu_preferences
        "group" -> android.R.drawable.ic_menu_myplaces
        "analytics" -> android.R.drawable.ic_menu_info_details
        
        else -> android.R.drawable.ic_menu_view
    }
}
