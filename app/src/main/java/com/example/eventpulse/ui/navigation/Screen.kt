package com.example.eventpulse.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Signup : Screen("signup")
    object UserHome : Screen("user_home")
    object EventDetail : Screen("event_detail/{eventId}") {
        fun createRoute(eventId: String) = "event_detail/$eventId"
    }
    object MyTickets : Screen("my_tickets")
    object OrganizerDashboard : Screen("organizer_dashboard")
    object CreateEditEvent : Screen("create_edit_event?eventId={eventId}") {
        fun createRoute(eventId: String? = null) = 
            if (eventId != null) "create_edit_event?eventId=$eventId" 
            else "create_edit_event"
    }
    object QrScanner : Screen("qr_scanner")
    object AdminDashboard : Screen("admin_dashboard")
}
