package com.example.eventpulse.ui.user

import android.app.Activity
import android.view.WindowManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.eventpulse.data.model.Booking
import com.example.eventpulse.ui.navigation.Screen
import com.example.eventpulse.utils.QrCodeGenerator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTicketsScreen(
    navController: NavHostController,
    viewModel: UserViewModel
) {
    val uiState by viewModel.userUiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var qrBookingToShow by remember { mutableStateOf<Booking?>(null) }
    var bookingToCancel by remember { mutableStateOf<Booking?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadBookings()
    }

    // QR Code Secure Dialog
    if (qrBookingToShow != null) {
        val booking = qrBookingToShow!!
        val context = LocalContext.current
        val activity = context as? Activity

        DisposableEffect(Unit) {
            activity?.window?.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
            onDispose {
                activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }
        }

        Dialog(onDismissRequest = { qrBookingToShow = null }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Your Event Pass",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = booking.eventTitle.ifEmpty { "Event Pass" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )

                    val qrBitmap = remember(booking.qrCodeHash, booking.id) {
                        QrCodeGenerator.generateForBooking(
                            bookingId = booking.id,
                            qrCodeHash = booking.qrCodeHash.ifEmpty { "TICKET_${booking.id}" },
                            width = 300,
                            height = 300
                        )
                    }

                    if (qrBitmap != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            modifier = Modifier
                                .size(240.dp)
                                .padding(8.dp)
                        ) {
                            Image(
                                bitmap = qrBitmap.asImageBitmap(),
                                contentDescription = "Ticket QR Code",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp)
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(200.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("QR Code unavailable", color = MaterialTheme.colorScheme.error)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Show this QR code at the venue gate",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Booking ID: ${booking.id}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { qrBookingToShow = null },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close Pass")
                    }
                }
            }
        }
    }

    // Cancel Booking Confirmation Dialog
    if (bookingToCancel != null) {
        val booking = bookingToCancel!!
        AlertDialog(
            onDismissRequest = { bookingToCancel = null },
            title = { Text("Cancel Booking?") },
            text = {
                Column {
                    Text("Are you sure you want to cancel your booking for:")
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = booking.eventTitle.ifEmpty { "Event ID: ${booking.eventIdString}" },
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "• Refund: ₹${booking.totalPrice.toInt()} will be credited back.\n" +
                               "• Policy: Cancellations are only permitted at least 48 hours before the event start.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    onClick = {
                        val idToCancel = booking.id
                        bookingToCancel = null
                        viewModel.cancelBooking(idToCancel) { success, message ->
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = message,
                                    withDismissAction = true
                                )
                            }
                        }
                    }
                ) {
                    Text("Confirm Cancellation")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { bookingToCancel = null }) {
                    Text("Keep Ticket")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Tickets") },
                actions = {
                    IconButton(onClick = { viewModel.loadBookings() }) {
                        Text("🔄", style = MaterialTheme.typography.titleMedium)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading && uiState.bookings.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.bookings.isEmpty() -> {
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
                            Text("🎟️", style = MaterialTheme.typography.displayMedium)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "No Tickets Yet",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Browse upcoming concerts, workshops, and meetups to book your first event.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(onClick = { navController.navigate(Screen.UserExplore.route) }) {
                                Text("Explore Events")
                            }
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(uiState.bookings) { booking ->
                            TicketItemCard(
                                booking = booking,
                                onViewQr = { qrBookingToShow = booking },
                                onCancel = { bookingToCancel = booking }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TicketItemCard(
    booking: Booking,
    onViewQr: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusUpper = booking.status.uppercase()
    val isConfirmed = statusUpper == "CONFIRMED"
    val isUsed = statusUpper == "USED"
    val isCancelled = statusUpper == "CANCELLED"

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Title and Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = booking.eventTitle.ifEmpty { "Event ID: ${booking.eventIdString}" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (booking.eventDate.isNotEmpty()) {
                        Text(
                            text = "📅 ${booking.eventDate}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    if (booking.eventLocation.isNotEmpty()) {
                        Text(
                            text = "📍 ${booking.eventLocation}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when {
                        isConfirmed -> MaterialTheme.colorScheme.primaryContainer
                        isUsed -> Color(0xFF10B981).copy(alpha = 0.2f)
                        isCancelled -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = when {
                            isConfirmed -> "CONFIRMED"
                            isUsed -> "CHECKED IN"
                            isCancelled -> "CANCELLED"
                            else -> booking.status
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isConfirmed -> MaterialTheme.colorScheme.primary
                            isUsed -> Color(0xFF10B981)
                            isCancelled -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            // Details row: Tickets count & Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${booking.ticketCount} Ticket${if (booking.ticketCount > 1) "s" else ""}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Total: ₹${booking.totalPrice.toInt()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = "ID: ${booking.id}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!isCancelled) {
                    Button(
                        onClick = onViewQr,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (isUsed) "View Pass" else "Show QR Pass")
                    }
                }

                if (isConfirmed) {
                    OutlinedButton(
                        onClick = onCancel,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}
