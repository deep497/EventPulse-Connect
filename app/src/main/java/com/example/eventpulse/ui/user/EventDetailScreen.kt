package com.example.eventpulse.ui.user

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun EventDetailScreen(
    navController: NavHostController,
    eventId: String,
    viewModel: UserViewModel
) {
    val uiState by viewModel.userUiState.collectAsStateWithLifecycle()
    
    LaunchedEffect(eventId) {
        viewModel.loadEventById(eventId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.selectedEvent != null) {
            val event = uiState.selectedEvent!!
            
            Text(
                text = "Event Details",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(event.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(event.description)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Date: ${event.date}")
                    Text("Location: ${event.location}")
                    Text("Price: $${event.price}")
                    Text("Available Seats: ${event.availableSeats}")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { /* Book tickets */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Book Tickets")
            }
        } else {
            Text("Event not found")
        }
    }
}
