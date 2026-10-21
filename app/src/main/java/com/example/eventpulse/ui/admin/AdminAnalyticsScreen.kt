package com.example.eventpulse.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.eventpulse.ui.components.DarkLightModeToggle
import com.example.eventpulse.ui.components.GlassCard
import com.example.eventpulse.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAnalyticsScreen(
    navController: NavHostController,
    viewModel: AdminViewModel
) {
    val uiState by viewModel.adminUiState.collectAsStateWithLifecycle()
    val isDark = LocalThemeIsDark.current

    LaunchedEffect(Unit) {
        viewModel.loadDashboardData()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) DarkBackgroundGradient else LightBackgroundGradient)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Platform Analytics",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isDark) DarkTextPrimary else LightTextPrimary
                        )
                        Text(
                            text = "Real-time Metrics from MongoDB",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isDark) DarkTextSecondary else LightTextSecondary
                        )
                    }

                    DarkLightModeToggle()
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Total Platform Revenue
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Text(
                                text = "Gross Platform Revenue",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isDark) DarkTextSecondary else LightTextSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "₹${String.format("%.2f", uiState.totalRevenue)}",
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) NeonCyan else ElectricPurple
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${uiState.totalBookings} Total Ticket Bookings",
                                style = MaterialTheme.typography.bodySmall,
                                color = StatusPublished
                            )
                        }
                    }
                }

                // Breakdown Grid: Users & Events
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Users Card
                        GlassCard(modifier = Modifier.weight(1f)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Registered Users", style = MaterialTheme.typography.bodySmall, color = if (isDark) DarkTextSecondary else LightTextSecondary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = uiState.totalUsers.toString(),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) DarkTextPrimary else LightTextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Platform Community", style = MaterialTheme.typography.labelSmall, color = StatusPublished)
                            }
                        }

                        // Events Card
                        GlassCard(modifier = Modifier.weight(1f)) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Total Events", style = MaterialTheme.typography.bodySmall, color = if (isDark) DarkTextSecondary else LightTextSecondary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = uiState.totalEvents.toString(),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) DarkTextPrimary else LightTextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("${uiState.pendingEvents.size} Pending Review", style = MaterialTheme.typography.labelSmall, color = StatusPending)
                            }
                        }
                    }
                }

                // Moderation Status Card
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Text(
                                text = "Platform Health Status",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) DarkTextPrimary else LightTextPrimary
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Published Events", style = MaterialTheme.typography.bodyMedium, color = if (isDark) DarkTextSecondary else LightTextSecondary)
                                Text("${uiState.publishedEventsCount}", fontWeight = FontWeight.Bold, color = StatusPublished)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = if (isDark) DarkGlassBorderStart else LightGlassBorder)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Pending Moderation", style = MaterialTheme.typography.bodyMedium, color = if (isDark) DarkTextSecondary else LightTextSecondary)
                                Text("${uiState.pendingEvents.size}", fontWeight = FontWeight.Bold, color = StatusPending)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = if (isDark) DarkGlassBorderStart else LightGlassBorder)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Confirmed Bookings", style = MaterialTheme.typography.bodyMedium, color = if (isDark) DarkTextSecondary else LightTextSecondary)
                                Text("${uiState.totalBookings}", fontWeight = FontWeight.Bold, color = if (isDark) NeonCyan else ElectricPurple)
                            }
                        }
                    }
                }
            }
        }
    }
}
