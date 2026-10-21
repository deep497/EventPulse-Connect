package com.example.eventpulse.ui.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserFavoritesScreen(
    navController: NavHostController,
    viewModel: UserViewModel
) {
    val uiState by viewModel.userUiState.collectAsStateWithLifecycle()

    val favoriteEvents = remember(uiState.recommendedEvents, uiState.favoriteEventIds) {
        uiState.recommendedEvents.filter { uiState.favoriteEventIds.contains(it.id) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Favorites") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (favoriteEvents.isEmpty()) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "🤍",
                    fontSize = 54.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No Favorites Yet",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap the heart icon on any event to save it here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.weight(1f))
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(favoriteEvents) { event ->
                        NeonEventCard(
                            event = event,
                            isFavorite = true,
                            onToggleFavorite = { viewModel.toggleFavorite(event.id) },
                            onClick = {
                                navController.navigate(com.example.eventpulse.ui.navigation.Screen.EventDetail.createRoute(event.id))
                            }
                        )
                    }
                }
            }
        }
    }
}
