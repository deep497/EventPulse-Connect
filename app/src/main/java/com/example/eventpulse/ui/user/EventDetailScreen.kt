package com.example.eventpulse.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.eventpulse.ui.components.GlassCard
import com.example.eventpulse.ui.navigation.Screen
import com.example.eventpulse.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    navController: NavHostController,
    eventId: String,
    viewModel: UserViewModel
) {
    val uiState by viewModel.userUiState.collectAsStateWithLifecycle()
    var ticketQuantity by remember { mutableIntStateOf(1) }
    val isDark = LocalThemeIsDark.current

    LaunchedEffect(eventId) {
        viewModel.loadEventById(eventId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Event Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Text(
                            text = "←",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) UserDarkTextPrimary else UserLightTextPrimary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading && uiState.selectedEvent == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = CoralPrimary)
            }
        } else if (uiState.selectedEvent != null) {
            val event = uiState.selectedEvent!!
            val totalPrice = event.price * ticketQuantity
            val bannerGradient = if (isDark) SunsetDarkHeroGradient else SunsetHeroGradient

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Scrollable content area
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Event Banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(bannerGradient)
                    ) {
                        if (event.imageUrl.isNotBlank()) {
                            AsyncImage(
                                model = event.imageUrl,
                                contentDescription = event.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🎟️", fontSize = 54.sp)
                            }
                        }

                        // Category pill overlaid on banner
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xCC000000),
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = event.category.ifBlank { "Event" },
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Title & Pricing Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = event.title,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isDark) UserDarkTextPrimary else UserLightTextPrimary
                            )
                            if (event.availableSeats > 0) {
                                Text(
                                    text = "🔥 ${event.availableSeats} seats available",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CoralPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = CoralPrimary.copy(alpha = 0.15f),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(
                                text = if (event.price > 0) "₹${event.price.toInt()}" else "Free",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = CoralPrimary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    HorizontalDivider()

                    // Date & Location Info
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("📅", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Date & Time",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isDark) UserDarkTextSecondary else UserLightTextSecondary
                                    )
                                    Text(
                                        text = event.date.ifBlank { "Date TBA" },
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            HorizontalDivider(color = Color(0x22888888))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("📍", fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Location / Venue",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isDark) UserDarkTextSecondary else UserLightTextSecondary
                                    )
                                    Text(
                                        text = event.location.ifBlank { "Venue TBA" },
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    // About Event Description
                    Text(
                        text = "About This Event",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) UserDarkTextPrimary else UserLightTextPrimary
                    )
                    Text(
                        text = event.description.ifBlank { "No description provided." },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDark) UserDarkTextSecondary else UserLightTextSecondary,
                        lineHeight = 22.sp
                    )
                }

                // Bottom Ticket Booking Card
                Surface(
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    color = if (isDark) Color(0xFF1F1722) else Color(0xFFFFF9F8),
                    tonalElevation = 8.dp,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Ticket Quantity Stepper
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Select Tickets",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Max ${event.availableSeats.coerceAtMost(10)} per order",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isDark) UserDarkTextSecondary else UserLightTextSecondary
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                FilledIconButton(
                                    onClick = { if (ticketQuantity > 1) ticketQuantity-- },
                                    enabled = ticketQuantity > 1,
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = if (isDark) Color(0xFF382736) else Color(0xFFFFECE7)
                                    )
                                ) {
                                    Text(
                                        text = "−",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = if (ticketQuantity > 1) CoralPrimary else Color.Gray
                                    )
                                }

                                Text(
                                    text = ticketQuantity.toString(),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.widthIn(min = 28.dp)
                                )

                                FilledIconButton(
                                    onClick = {
                                        if (ticketQuantity < event.availableSeats && ticketQuantity < 10) {
                                            ticketQuantity++
                                        }
                                    },
                                    enabled = ticketQuantity < event.availableSeats && ticketQuantity < 10,
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = if (isDark) Color(0xFF382736) else Color(0xFFFFECE7)
                                    )
                                ) {
                                    Text(
                                        text = "+",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = if (ticketQuantity < event.availableSeats) CoralPrimary else Color.Gray
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = Color(0x22888888))

                        // Total Price & Checkout Button Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Total Price",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isDark) UserDarkTextSecondary else UserLightTextSecondary
                                )
                                Text(
                                    text = if (totalPrice > 0) "₹${totalPrice.toInt()}" else "Free",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = CoralPrimary
                                )
                            }

                            Button(
                                onClick = {
                                    navController.navigate(Screen.Checkout.createRoute(eventId, ticketQuantity))
                                },
                                enabled = event.availableSeats > 0,
                                colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .height(48.dp)
                                    .padding(start = 16.dp)
                            ) {
                                Text(
                                    text = if (event.price == 0.0) "Register Free" else "Proceed to Checkout",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Event not found or unavailable.")
            }
        }
    }
}
