package com.example.eventpulse.ui.organizer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.eventpulse.data.model.Attendee
import com.example.eventpulse.ui.components.ExpiredBadge
import com.example.eventpulse.ui.components.expiredStyle
import com.example.eventpulse.ui.components.GlassCard
import com.example.eventpulse.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizerAttendeesScreen(
    navController: NavHostController,
    viewModel: OrganizerViewModel
) {
    val uiState by viewModel.organizerUiState.collectAsStateWithLifecycle()
    val isDark = LocalThemeIsDark.current
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Checked In", "Pending Entry")

    LaunchedEffect(Unit) {
        viewModel.loadAttendees()
    }

    val filteredAttendees = remember(uiState.attendees, selectedFilter) {
        when (selectedFilter) {
            "Checked In" -> uiState.attendees.filter { it.isCheckedIn }
            "Pending Entry" -> uiState.attendees.filter { !it.isCheckedIn }
            else -> uiState.attendees
        }
    }

    val checkedInCount = remember(uiState.attendees) {
        uiState.attendees.count { it.isCheckedIn }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Attendee Management", fontWeight = FontWeight.Bold)
                        Text(
                            text = "${uiState.attendees.size} total attendees • $checkedInCount checked in",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadAttendees() }) {
                        Text("🔄", style = MaterialTheme.typography.titleMedium)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filter Pills Row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filters) { filter ->
                    val isSelected = selectedFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filter },
                        label = {
                            val count = when (filter) {
                                "Checked In" -> checkedInCount
                                "Pending Entry" -> uiState.attendees.size - checkedInCount
                                else -> uiState.attendees.size
                            }
                            Text("$filter ($count)")
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (isDark) RosePlum else RoyalPlum,
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                when {
                    uiState.isLoading && uiState.attendees.isEmpty() -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = if (isDark) RosePlum else RoyalPlum)
                        }
                    }
                    filteredAttendees.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("👥", style = MaterialTheme.typography.displayMedium)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = if (uiState.attendees.isEmpty()) "No Attendees Yet" else "No $selectedFilter Attendees",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (uiState.attendees.isEmpty())
                                        "When users book tickets for your events, they will automatically appear in this list with real-time check-in tracking."
                                    else
                                        "No attendees match the \"$selectedFilter\" filter right now.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    else -> {
                        val upcomingAttendees = remember(filteredAttendees) {
                            filteredAttendees.filter { !it.isExpired }
                        }
                        val pastAttendees = remember(filteredAttendees) {
                            filteredAttendees.filter { it.isExpired }
                        }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 20.dp, top = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Section 1: Upcoming Event Attendees
                            if (upcomingAttendees.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "Current & Upcoming Event Attendees (${upcomingAttendees.size})",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark) OrgDarkTextPrimary else OrgLightTextPrimary,
                                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                                    )
                                }
                                items(upcomingAttendees, key = { it.bookingId }) { attendee ->
                                    AttendeeCard(
                                        attendee = attendee,
                                        isPast = false,
                                        onCheckInNow = {
                                            viewModel.checkIn(bookingId = attendee.bookingId) { success, _ ->
                                                if (success) {
                                                    viewModel.loadAttendees()
                                                }
                                            }
                                        }
                                    )
                                }
                            }

                            // Section 2: Past Event Attendees & Attendance Summary
                            if (pastAttendees.isNotEmpty()) {
                                item {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    val totalBooked = pastAttendees.sumOf { it.ticketCount }
                                    val attended = pastAttendees.count { it.isCheckedIn || it.checkedIn }
                                    val noShows = (pastAttendees.size - attended).coerceAtLeast(0)

                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Text(
                                            text = "Past Events Attendees (${pastAttendees.size})",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDark) OrgDarkTextSecondary else OrgLightTextSecondary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                                horizontalArrangement = Arrangement.SpaceAround
                                            ) {
                                                Text(
                                                    text = "Booked: $totalBooked",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Text(
                                                    text = "Checked In: $attended",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color(0xFF10B981)
                                                )
                                                Text(
                                                    text = "No-Shows: $noShows",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }
                                    }
                                }
                                items(pastAttendees, key = { it.bookingId }) { attendee ->
                                    AttendeeCard(
                                        attendee = attendee,
                                        isPast = true,
                                        onCheckInNow = {}
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AttendeeCard(
    attendee: Attendee,
    isPast: Boolean,
    onCheckInNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalThemeIsDark.current
    val hasCheckedIn = attendee.isCheckedIn || attendee.checkedIn

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .expiredStyle(isPast)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: User Info and Check-in Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = attendee.userName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) OrgDarkTextPrimary else OrgLightTextPrimary
                    )
                    Text(
                        text = attendee.userEmail,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDark) OrgDarkTextSecondary else OrgLightTextSecondary
                    )
                    if (attendee.userPhone.isNotBlank()) {
                        Text(
                            text = "📞 ${attendee.userPhone}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isDark) OrgDarkTextSecondary else OrgLightTextSecondary
                        )
                    }
                }

                // Check-in Status Pill / Expired Tag
                if (isPast) {
                    if (hasCheckedIn) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.15f),
                            modifier = Modifier.padding(start = 8.dp)
                        ) {
                            Text(
                                text = "Checked In",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    } else {
                        ExpiredBadge(
                            text = "Did not attend",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (hasCheckedIn) Color(0xFF10B981).copy(alpha = 0.2f) else Color(0xFFF59E0B).copy(alpha = 0.2f),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = if (hasCheckedIn) "CHECKED IN" else "PENDING ENTRY",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (hasCheckedIn) Color(0xFF10B981) else Color(0xFFF59E0B),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = if (isDark) OrgDarkBorder else PlumBorder)

            // Event & Ticket Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "🎟️ ${attendee.eventTitle}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (attendee.eventDate.isNotBlank()) {
                        Text(
                            text = "📅 ${attendee.eventDate}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${attendee.ticketCount} Ticket${if (attendee.ticketCount > 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (attendee.totalPrice > 0) "₹${attendee.totalPrice.toInt()}" else "Free",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isPast) Color.Gray else (if (isDark) RosePlum else RoyalPlum)
                    )
                }
            }

            // Booking ID and Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ID: ${attendee.bookingId}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )

                if (!isPast && !hasCheckedIn) {
                    Button(
                        onClick = onCheckInNow,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDark) RosePlum else RoyalPlum
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("Check In", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
