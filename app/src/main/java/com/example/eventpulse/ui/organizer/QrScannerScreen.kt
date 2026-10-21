package com.example.eventpulse.ui.organizer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QrScannerScreen(
    navController: NavHostController,
    viewModel: OrganizerViewModel
) {
    var manualInput by remember { mutableStateOf("") }
    var isVerifying by remember { mutableStateOf(false) }

    var resultMessage by remember { mutableStateOf<String?>(null) }
    var isSuccessResult by remember { mutableStateOf(false) }
    var showResultDialog by remember { mutableStateOf(false) }

    val handleCheckIn: (String) -> Unit = { code ->
        val trimmed = code.trim()
        if (trimmed.isNotEmpty()) {
            isVerifying = true
            // If length is 64 hex characters, treat as qrCodeHash; otherwise bookingId
            val isHash = trimmed.length == 64 && trimmed.all { it in "0123456789abcdefABCDEF" }
            val qrCodeHash = if (isHash) trimmed else null
            val bookingId = if (!isHash) trimmed else null

            viewModel.checkIn(qrCodeHash = qrCodeHash, bookingId = bookingId) { success, msg ->
                isVerifying = false
                isSuccessResult = success
                resultMessage = msg
                showResultDialog = true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ticket Check-In") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_revert),
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Attendee Ticket Verification",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Camera preview placeholder
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "📷",
                            style = MaterialTheme.typography.displayMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Camera Scanner Preview",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Point camera at attendee's QR pass",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Manual Entry Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Manual Check-In by ID",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "If camera scanning fails, enter the 24-character Booking ID or QR hash directly:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = manualInput,
                        onValueChange = { manualInput = it },
                        label = { Text("Booking ID / QR Hash") },
                        placeholder = { Text("e.g. 660f... or hash") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Button(
                        onClick = { handleCheckIn(manualInput) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = manualInput.isNotBlank() && !isVerifying,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isVerifying) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Verifying...")
                        } else {
                            Text("Verify & Check In")
                        }
                    }
                }
            }

            // Development Quick Simulation
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Quick Test Shortcuts",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Paste a booking ID from MongoDB or click to simulate an invalid check-in test:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedButton(
                        onClick = { handleCheckIn("000000000000000000000000") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isVerifying
                    ) {
                        Text("Test Non-Existent Booking ID")
                    }
                }
            }
        }

        // Result Dialog (Success or Error)
        if (showResultDialog) {
            AlertDialog(
                onDismissRequest = { showResultDialog = false },
                icon = {
                    Text(
                        text = if (isSuccessResult) "✅" else "❌",
                        style = MaterialTheme.typography.displaySmall
                    )
                },
                title = {
                    Text(
                        text = if (isSuccessResult) "Check-In Approved!" else "Check-In Failed",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isSuccessResult) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = resultMessage ?: if (isSuccessResult) "Attendee entry approved." else "Unable to verify ticket.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (isSuccessResult) {
                            Text(
                                text = "Ticket marked as USED. The attendee is admitted.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        showResultDialog = false
                        if (isSuccessResult) {
                            manualInput = ""
                        }
                    }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}
