package com.example.eventpulse.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.eventpulse.ui.theme.*

@Composable
fun BottomNavigationBar(
    navController: NavController,
    items: List<BottomNavItem>
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isDark = LocalThemeIsDark.current

    NavigationBar(
        containerColor = if (isDark) DarkGlassCard else LightGlassCard,
        contentColor = if (isDark) DarkTextPrimary else LightTextPrimary,
        tonalElevation = 8.dp,
        modifier = Modifier
            .border(
                width = 1.dp,
                color = if (isDark) DarkGlassBorderStart.copy(alpha = 0.5f) else LightGlassBorder,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            )
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationBarItem(
                icon = { 
                    Icon(
                        painter = painterResource(id = getIconForRoute(item.icon)),
                        contentDescription = item.title,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = { Text(item.title) },
                selected = isSelected,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = if (isDark) NeonCyan else ElectricPurple,
                    selectedTextColor = if (isDark) NeonCyan else ElectricPurple,
                    unselectedIconColor = if (isDark) DarkTextTertiary else LightTextTertiary,
                    unselectedTextColor = if (isDark) DarkTextTertiary else LightTextTertiary,
                    indicatorColor = if (isDark) Color(0x3300F5D4) else Color(0x337B2CBF)
                ),
                onClick = {
                    navController.navigate(item.route) {
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
