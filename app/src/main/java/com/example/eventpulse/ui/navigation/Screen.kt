package com.example.eventpulse.ui.navigation

sealed class Screen(val route: String) {
    // Auth screens
    object Login : Screen("login")
    object Signup : Screen("signup")
    
    // User bottom nav screens
    object UserExplore : Screen("user_explore")
    object UserFavorites : Screen("user_favorites")
    object UserTickets : Screen("user_tickets")
    object UserProfile : Screen("user_profile")
    
    // User detail screens
    object EventDetail : Screen("event_detail/{eventId}") {
        fun createRoute(eventId: String) = "event_detail/$eventId"
    }

    object Checkout : Screen("checkout/{eventId}?quantity={quantity}") {
        fun createRoute(eventId: String, quantity: Int = 1) = "checkout/$eventId?quantity=$quantity"
    }
    
    // Organizer bottom nav screens
    object OrgDashboard : Screen("org_dashboard")
    object OrgEvents : Screen("org_events")
    object OrgAttendees : Screen("org_attendees")
    object OrgSettings : Screen("org_settings")
    
    // Organizer detail screens
    object CreateEditEvent : Screen("create_edit_event?eventId={eventId}") {
        fun createRoute(eventId: String? = null) = 
            if (eventId != null) "create_edit_event?eventId=$eventId" 
            else "create_edit_event"
    }
    object QrScanner : Screen("qr_scanner")
    
    // Admin bottom nav screens
    object AdminModeration : Screen("admin_moderation")
    object AdminUsers : Screen("admin_users")
    object AdminAnalytics : Screen("admin_analytics")
    
    // Legacy routes (for backward compatibility)
    object UserHome : Screen("user_explore")
    object MyTickets : Screen("user_tickets")
    object OrganizerDashboard : Screen("org_dashboard")
    object AdminDashboard : Screen("admin_moderation")
}

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: String
) {
    // User bottom nav items
    object UserExplore : BottomNavItem("user_explore", "Explore", "explore")
    object UserFavorites : BottomNavItem("user_favorites", "Favorites", "favorite")
    object UserTickets : BottomNavItem("user_tickets", "Tickets", "confirmation_number")
    object UserProfile : BottomNavItem("user_profile", "Profile", "person")
    
    // Organizer bottom nav items
    object OrgDashboard : BottomNavItem("org_dashboard", "Dashboard", "dashboard")
    object OrgEvents : BottomNavItem("org_events", "Events", "event")
    object OrgAttendees : BottomNavItem("org_attendees", "Attendees", "people")
    object OrgSettings : BottomNavItem("org_settings", "Settings", "settings")
    
    // Admin bottom nav items
    object AdminModeration : BottomNavItem("admin_moderation", "Moderation", "gavel")
    object AdminUsers : BottomNavItem("admin_users", "Users", "group")
    object AdminAnalytics : BottomNavItem("admin_analytics", "Analytics", "analytics")
}

fun getUserBottomNavItems() = listOf(
    BottomNavItem.UserExplore,
    BottomNavItem.UserFavorites,
    BottomNavItem.UserTickets,
    BottomNavItem.UserProfile
)

fun getOrganizerBottomNavItems() = listOf(
    BottomNavItem.OrgDashboard,
    BottomNavItem.OrgEvents,
    BottomNavItem.OrgAttendees,
    BottomNavItem.OrgSettings
)

fun getAdminBottomNavItems() = listOf(
    BottomNavItem.AdminModeration,
    BottomNavItem.AdminUsers,
    BottomNavItem.AdminAnalytics
)
