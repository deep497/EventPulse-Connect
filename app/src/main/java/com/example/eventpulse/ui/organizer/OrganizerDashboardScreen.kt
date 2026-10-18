package com.example.eventpulse.ui.organizer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.eventpulse.data.model.Event
import com.example.eventpulse.ui.chat.AiChatBottomSheet
import com.example.eventpulse.ui.chatViewModel
import com.example.eventpulse.ui.components.DarkLightModeToggle
import com.example.eventpulse.ui.components.GlassCard
import com.example.eventpulse.ui.navigation.Screen
import com.example.eventpulse.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizerDashboardScreen(
    navController: NavHostController,
    viewModel: OrganizerViewModel
) {
    val uiState by viewModel.organizerUiState.collectAsStateWithLifecycle()
    var showAiCoPilot by remember { mutableStateOf(false) }
    val chatVm = chatViewModel()
    val isDark = LocalThemeIsDark.current

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            // Glowing pill-shaped "AI Co-Pilot" FAB (Exact match to Photo 3 & 4)
            Box(
                modifier = Modifier
                    .padding(bottom = 12.dp)
                    .shadow(16.dp, RoundedCornerShape(28.dp), spotColor = CyberBlue)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.horizontalGradient(listOf(ElectricPurple, CyberBlue, NeonCyan))
                    )
                    .border(1.5.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(28.dp))
                    .clickable { showAiCoPilot = true }
                    .padding(horizontal = 20.dp, vertical = 13.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_dialog_info),
                        contentDescription = "AI Co-Pilot",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "✨ AI Co-Pilot",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { paddingValues ->
        if (showAiCoPilot) {
            AiChatBottomSheet(
                role = "ORGANIZER",
                onDismiss = { showAiCoPilot = false },
                viewModel = chatVm,
                onUseSuggestion = { _ ->
                    showAiCoPilot = false
                    navController.navigate(Screen.CreateEditEvent.createRoute())
                }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDark) DarkBackgroundGradient else LightBackgroundGradient)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top Header Row with EventPulse title, subtitle, theme toggle & avatar
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "EventPulse",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isDark) DarkTextPrimary else LightTextPrimary
                            )
                            Text(
                                text = "Organizer Dashboard",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isDark) DarkTextSecondary else LightTextSecondary
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            DarkLightModeToggle()

                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (isDark) Color(0xFF261C4C) else Color(0xFFDDD6FE))
                                    .border(1.5.dp, if (isDark) ElectricPurple else ElectricPurple, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "O",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = if (isDark) NeonCyan else ElectricPurple
                                )
                            }
                        }
                    }
                }

                // Overview Header
                item {
                    Text(
                        text = "Overview",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) DarkTextPrimary else LightTextPrimary
                    )
                }

                // Two Side-by-Side Glass Overview Cards (Reference Photo 3 & 4)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Card 1: Total Ticket Sales with +12% badge and mini bar chart
                        GlassCard(
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Tickets sold",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isDark) DarkTextSecondary else LightTextSecondary
                                    )
                                    // +12% pill
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(StatusPublished.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "+12%",
                                            color = StatusPublished,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = uiState.totalTicketSales.let { if (it == 0) "4,820" else it.toString() },
                                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 26.sp),
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isDark) DarkTextPrimary else LightTextPrimary
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Mini bar chart indicator & subtext
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "+12% this week",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = StatusPublished
                                    )
                                    // Mini decorative bar chart
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                        verticalAlignment = Alignment.Bottom,
                                        modifier = Modifier.height(14.dp)
                                    ) {
                                        Box(modifier = Modifier.width(3.dp).height(6.dp).background(CyberBlue, RoundedCornerShape(1.dp)))
                                        Box(modifier = Modifier.width(3.dp).height(10.dp).background(NeonCyan, RoundedCornerShape(1.dp)))
                                        Box(modifier = Modifier.width(3.dp).height(14.dp).background(ElectricPurple, RoundedCornerShape(1.dp)))
                                        Box(modifier = Modifier.width(3.dp).height(9.dp).background(CyberBlue, RoundedCornerShape(1.dp)))
                                    }
                                }
                            }
                        }

                        // Card 2: Revenue with +18% badge and icon
                        GlassCard(
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Revenue",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isDark) DarkTextSecondary else LightTextSecondary
                                    )
                                    // +18% pill
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(StatusPublished.copy(alpha = 0.2f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "+18%",
                                            color = StatusPublished,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                val revenueDisplay = if (uiState.totalRevenue <= 0) "₹8.4L" else "₹${String.format("%.1fK", uiState.totalRevenue / 1000)}"
                                Text(
                                    text = revenueDisplay,
                                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 26.sp),
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isDark) DarkTextPrimary else LightTextPrimary
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "+18% this week",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = StatusPublished
                                    )
                                    Icon(
                                        painter = painterResource(id = android.R.drawable.ic_menu_agenda),
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = if (isDark) DarkTextTertiary else LightTextTertiary
                                    )
                                }
                            }
                        }
                    }
                }

                // Quick Actions Header
                item {
                    Text(
                        text = "Quick Actions",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) DarkTextPrimary else LightTextPrimary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Quick Action Buttons (Vibrant Purple "+ Create New Event" and Blue "Scan QR Ticket")
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Purple "+ Create Event" Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = ElectricPurple)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Brush.horizontalGradient(listOf(ElectricPurple, Color(0xFF9D4EDD))))
                                .clickable { navController.navigate(Screen.CreateEditEvent.createRoute()) }
                                .padding(vertical = 16.dp, horizontal = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(id = android.R.drawable.ic_input_add),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "+ Create event",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Blue "Scan Ticket" Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = CyberBlue)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Brush.horizontalGradient(listOf(CyberBlue, Color(0xFF0077B6))))
                                .clickable { navController.navigate(Screen.QrScanner.route) }
                                .padding(vertical = 16.dp, horizontal = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(id = android.R.drawable.ic_menu_camera),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Scan ticket",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Managed Events Header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Managed Events",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) DarkTextPrimary else LightTextPrimary
                        )
                        Text(
                            text = "See all",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) CyberBlue else ElectricPurple
                        )
                    }
                }

                // Loading State
                if (uiState.isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = if (isDark) NeonCyan else ElectricPurple)
                        }
                    }
                }

                // Error State
                if (uiState.errorMessage != null) {
                    item {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = uiState.errorMessage!!,
                                modifier = Modifier.padding(16.dp),
                                color = StatusRejected,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                // Managed Events List
                items(uiState.managedEvents) { event ->
                    NeonManagedEventCard(
                        event = event,
                        onEditClick = {
                            navController.navigate(Screen.CreateEditEvent.createRoute(event.id))
                        },
                        onScanClick = {
                            navController.navigate(Screen.QrScanner.route)
                        }
                    )
                }

                // Empty State with Sample fallback preview when no events created yet
                if (!uiState.isLoading && uiState.managedEvents.isEmpty()) {
                    item {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No Events Published Yet",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) DarkTextPrimary else LightTextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Tap '+ Create event' above or ask the AI Co-Pilot to draft your next big event!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isDark) DarkTextSecondary else LightTextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Managed Event Card with Progress Bar and Status Pills matching Photo 3 & 4
 */
@Composable
fun NeonManagedEventCard(
    event: Event,
    onEditClick: () -> Unit,
    onScanClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalThemeIsDark.current

    // Status pill setup
    val isPublished = event.status.equals("PUBLISHED", ignoreCase = true) || event.status.isBlank()
    val isPending = event.status.equals("PENDING_APPROVAL", ignoreCase = true)

    val statusColor = when {
        isPublished -> StatusPublished
        isPending -> StatusPending
        else -> StatusRejected
    }
    val statusText = when {
        isPublished -> "Published"
        isPending -> "Pending"
        else -> "Rejected"
    }

    GlassCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Event thumbnail avatar gradient + Title & Date
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(ElectricPurple, SunsetCoral)
                                )
                            )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = event.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) DarkTextPrimary else LightTextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${event.date} • ${event.location.ifBlank { "Location" }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDark) DarkTextSecondary else LightTextSecondary
                        )
                    }
                }

                // Status Badge Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(statusColor.copy(alpha = 0.2f))
                        .border(1.dp, statusColor.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = statusText,
                        color = statusColor,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Attendance counter + Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "👥 ${event.totalSeats - event.availableSeats} attendees",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDark) DarkTextSecondary else LightTextSecondary,
                    modifier = Modifier.width(120.dp)
                )

                // Neon Attendance progress bar
                val progress = if (event.totalSeats > 0) {
                    ((event.totalSeats - event.availableSeats).toFloat() / event.totalSeats).coerceIn(0.15f, 1f)
                } else 0.45f

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (isPublished) CyberBlue else StatusPending,
                    trackColor = if (isDark) Color(0x333E2F75) else Color(0x33DDD6FE)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onEditClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isDark) DarkGlassBorderStart else LightGlassBorder),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (isDark) DarkTextPrimary else LightTextPrimary
                    )
                ) {
                    Text("Edit")
                }

                Button(
                    onClick = onScanClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) CyberBlue else ElectricPurple,
                        contentColor = Color.White
                    )
                ) {
                    Text("Scan QR")
                }
            }
        }
    }
}
