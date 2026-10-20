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
import com.example.eventpulse.ui.authViewModel
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
    val authVm = authViewModel()
    val authState by authVm.authUiState.collectAsStateWithLifecycle()
    var showAiCoPilot by remember { mutableStateOf(false) }
    val chatVm = chatViewModel()
    val isDark = LocalThemeIsDark.current

    val orgInitial = (authState.fullName.ifBlank { authState.user?.name ?: "R" }).first().uppercase()

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            // Floating pill "AI Co-Pilot" matching reference image
            Box(
                modifier = Modifier
                    .padding(bottom = 12.dp)
                    .shadow(10.dp, RoundedCornerShape(26.dp), spotColor = if (isDark) RosePlum else RoyalPlum)
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        if (isDark) PlumRoseGradient
                        else Brush.horizontalGradient(listOf(RoyalPlum, Color(0xFF9C27B0)))
                    )
                    .clickable { showAiCoPilot = true }
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_dialog_info),
                        contentDescription = "AI Co-Pilot",
                        tint = if (isDark) Color(0xFF171219) else Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "✨ AI Co-Pilot",
                        color = if (isDark) Color(0xFF171219) else Color.White,
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
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
                                color = if (isDark) OrgDarkTextPrimary else OrgLightTextPrimary
                            )
                            Text(
                                text = "Organiser dashboard",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isDark) OrgDarkTextSecondary else OrgLightTextSecondary
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            DarkLightModeToggle()

                            // Organizer Avatar Circle (Clicking opens Settings/Profile)
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (isDark) Color(0xFF382337) else RoyalPlum)
                                    .border(1.5.dp, if (isDark) RosePlum else RoyalPlum, CircleShape)
                                    .clickable { navController.navigate(Screen.OrgSettings.route) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = orgInitial,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = Color.White
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
                        color = if (isDark) OrgDarkTextPrimary else OrgLightTextPrimary
                    )
                }

                // Two Side-by-Side Overview Cards (Real dynamic metrics, 0 when empty)
                item {
                    val totalTickets = uiState.totalTicketSales
                    val revenue = uiState.totalRevenue

                    val ticketsSoldDisplay = totalTickets.toString()
                    val salesBadge = if (totalTickets > 0) "+12%" else "0%"
                    val salesSubtext = if (totalTickets > 0) "+12% this week" else "No sales yet"

                    val revenueDisplay = if (revenue <= 0) {
                        "₹0"
                    } else if (revenue >= 100000) {
                        "₹${String.format("%.1fL", revenue / 100000)}"
                    } else if (revenue >= 1000) {
                        "₹${String.format("%.1fK", revenue / 1000)}"
                    } else {
                        "₹${revenue.toInt()}"
                    }
                    val revBadge = if (revenue > 0) "+18%" else "0%"
                    val revSubtext = if (revenue > 0) "+18% this week" else "No revenue yet"

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Card 1: Tickets sold
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
                                        color = if (isDark) OrgDarkTextSecondary else OrgLightTextSecondary
                                    )
                                    // Status pill
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (totalTickets > 0) StatusPublishedBg else Color(0x1A888888))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = salesBadge,
                                            color = if (totalTickets > 0) StatusPublished else Color.Gray,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = ticketsSoldDisplay,
                                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 26.sp),
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isDark) OrgDarkTextPrimary else OrgLightTextPrimary
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = salesSubtext,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (totalTickets > 0) StatusPublished else (if (isDark) OrgDarkTextTertiary else OrgLightTextTertiary)
                                    )
                                    if (totalTickets > 0) {
                                        // Mini decorative bar chart
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                                            verticalAlignment = Alignment.Bottom,
                                            modifier = Modifier.height(14.dp)
                                        ) {
                                            Box(modifier = Modifier.width(3.dp).height(6.dp).background(RosePlum, RoundedCornerShape(1.dp)))
                                            Box(modifier = Modifier.width(3.dp).height(10.dp).background(RoyalPlum, RoundedCornerShape(1.dp)))
                                            Box(modifier = Modifier.width(3.dp).height(14.dp).background(RosePlum, RoundedCornerShape(1.dp)))
                                            Box(modifier = Modifier.width(3.dp).height(9.dp).background(RoyalPlum, RoundedCornerShape(1.dp)))
                                        }
                                    }
                                }
                            }
                        }

                        // Card 2: Revenue
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
                                        color = if (isDark) OrgDarkTextSecondary else OrgLightTextSecondary
                                    )
                                    // Status pill
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (revenue > 0) StatusPublishedBg else Color(0x1A888888))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = revBadge,
                                            color = if (revenue > 0) StatusPublished else Color.Gray,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = revenueDisplay,
                                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 26.sp),
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isDark) OrgDarkTextPrimary else OrgLightTextPrimary
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = revSubtext,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (revenue > 0) StatusPublished else (if (isDark) OrgDarkTextTertiary else OrgLightTextTertiary)
                                    )
                                    Icon(
                                        painter = painterResource(id = android.R.drawable.ic_menu_agenda),
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = if (isDark) OrgDarkTextTertiary else OrgLightTextTertiary
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
                        color = if (isDark) OrgDarkTextPrimary else OrgLightTextPrimary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Quick Action Buttons (Sunset Coral / Plum spec)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Solid Plum "+ Create Event" Button
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .shadow(6.dp, RoundedCornerShape(14.dp), spotColor = if (isDark) RosePlum else RoyalPlum)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isDark) Brush.horizontalGradient(listOf(RosePlum, Color(0xFFC0567A)))
                                    else Brush.horizontalGradient(listOf(RoyalPlum, Color(0xFF8E24AA)))
                                )
                                .clickable { navController.navigate(Screen.CreateEditEvent.createRoute()) }
                                .padding(vertical = 14.dp, horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(id = android.R.drawable.ic_input_add),
                                    contentDescription = null,
                                    tint = if (isDark) Color(0xFF18131A) else Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "+ Create event",
                                    color = if (isDark) Color(0xFF18131A) else Color.White,
                                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // "Scan Ticket" Button (Tinted plum with border in light mode)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .shadow(if (isDark) 4.dp else 2.dp, RoundedCornerShape(14.dp))
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isDark) OrgDarkCard else PlumLightTint)
                                .border(1.dp, if (isDark) OrgDarkBorder else PlumBorder, RoundedCornerShape(14.dp))
                                .clickable { navController.navigate(Screen.QrScanner.route) }
                                .padding(vertical = 14.dp, horizontal = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painter = painterResource(id = android.R.drawable.ic_menu_camera),
                                    contentDescription = null,
                                    tint = if (isDark) Color.White else RoyalPlum,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Scan ticket",
                                    color = if (isDark) Color.White else RoyalPlum,
                                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp),
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
                            color = if (isDark) OrgDarkTextPrimary else OrgLightTextPrimary
                        )
                        Text(
                            text = "See all",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) RosePlum else RoyalPlum,
                            modifier = Modifier.clickable { navController.navigate(Screen.OrgEvents.route) }
                        )
                    }
                }

                // Loading State
                if (uiState.isLoading) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = if (isDark) RosePlum else RoyalPlum)
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

                // Empty State or List of Managed Events
                if (uiState.managedEvents.isEmpty() && !uiState.isLoading) {
                    item {
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp, horizontal = 20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .background(if (isDark) Color(0xFF382337) else PlumLightTint),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = android.R.drawable.ic_menu_today),
                                        contentDescription = null,
                                        modifier = Modifier.size(30.dp),
                                        tint = if (isDark) RosePlum else RoyalPlum
                                    )
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "No Events Published Yet",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) OrgDarkTextPrimary else OrgLightTextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Tap '+ Create event' above or ask the AI Co-Pilot to craft your next big event!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isDark) OrgDarkTextSecondary else OrgLightTextSecondary,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
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
                }

                item {
                    Spacer(modifier = Modifier.height(70.dp))
                }
            }
        }
    }
}

/**
 * Managed Event Card with Plum/Rose accents and status pills.
 */
@Composable
fun NeonManagedEventCard(
    event: Event,
    onEditClick: () -> Unit,
    onScanClick: () -> Unit
) {
    val isDark = LocalThemeIsDark.current
    val isPublished = event.status == "PUBLISHED"
    val isPending = event.status == "PENDING_APPROVAL"

    val statusColor = when {
        isPublished -> StatusPublished
        isPending -> StatusPending
        else -> StatusRejected
    }

    val statusBg = when {
        isPublished -> StatusPublishedBg
        isPending -> StatusPendingBg
        else -> StatusRejectedBg
    }

    val statusText = when {
        isPublished -> "Published"
        isPending -> "Pending"
        else -> event.status
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Event Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Event thumbnail plum gradient + Title & Date
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isDark) Brush.verticalGradient(listOf(RosePlum, RoyalPlum))
                                else Brush.verticalGradient(listOf(RoyalPlum, Color(0xFF9C27B0)))
                            )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = event.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) OrgDarkTextPrimary else OrgLightTextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${event.date} • ${event.location.ifBlank { "Venue" }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDark) OrgDarkTextSecondary else OrgLightTextSecondary
                        )
                    }
                }

                // Status Badge Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(statusBg)
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
            val attendeesCount = (event.totalSeats - event.availableSeats).coerceAtLeast(0)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "👥 $attendeesCount attendees",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDark) OrgDarkTextSecondary else OrgLightTextSecondary,
                    modifier = Modifier.width(120.dp)
                )

                val progress = if (event.totalSeats > 0) {
                    (attendeesCount.toFloat() / event.totalSeats).coerceIn(0f, 1f)
                } else 0f

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (isDark) RosePlum else RoyalPlum,
                    trackColor = if (isDark) Color(0xFF382337) else PlumBorder
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
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isDark) OrgDarkBorder else PlumBorder),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (isDark) OrgDarkTextPrimary else RoyalPlum
                    )
                ) {
                    Text("Edit")
                }

                Button(
                    onClick = onScanClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) RosePlum else RoyalPlum,
                        contentColor = if (isDark) Color(0xFF171219) else Color.White
                    )
                ) {
                    Text("Scan QR")
                }
            }
        }
    }
}
