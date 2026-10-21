package com.example.eventpulse.ui.organizer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.eventpulse.data.model.Event

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEditEventScreen(
    navController: NavHostController,
    eventId: String? = null,
    viewModel: OrganizerViewModel
) {
    val uiState by viewModel.organizerUiState.collectAsStateWithLifecycle()
    val isEditMode = !eventId.isNullOrBlank()

    val existingEvent = remember(eventId, uiState.managedEvents) {
        if (isEditMode) uiState.managedEvents.find { it.id == eventId } else null
    }

    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Music") }
    var description by remember { mutableStateOf("") }
    var venue by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var capacity by remember { mutableStateOf("") }
    var bannerUrl by remember { mutableStateOf("") }
    var expandedCategory by remember { mutableStateOf(false) }

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // Pre-populate if editing
    LaunchedEffect(existingEvent) {
        existingEvent?.let { e ->
            title = e.title
            category = e.category.ifBlank { "Music" }
            description = e.description
            venue = e.location
            date = e.date
            price = if (e.price > 0) e.price.toInt().toString() else "0"
            capacity = if (e.totalSeats > 0) e.totalSeats.toString() else "100"
            bannerUrl = e.imageUrl
        }
    }

    val categories = listOf("Music", "Tech", "Art", "Sports", "Business", "Other")

    // Delete confirmation dialog
    if (showDeleteConfirmDialog && eventId != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Event?") },
            text = {
                Text("Are you sure you want to delete \"${title.ifEmpty { "this event" }}\"? All confirmed bookings will be cancelled and held amounts will be refunded.")
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    onClick = {
                        showDeleteConfirmDialog = false
                        viewModel.deleteEvent(eventId)
                        navController.popBackStack()
                    }
                ) {
                    Text("Delete Event")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Edit Event" else "Create Event") },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (isEditMode) "Update event details (will require re-approval)" else "Fill in event details for admin approval",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Event Title *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Category Dropdown
            ExposedDropdownMenuBox(
                expanded = expandedCategory,
                onExpandedChange = { expandedCategory = it }
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(12.dp)
                )

                ExposedDropdownMenu(
                    expanded = expandedCategory,
                    onDismissRequest = { expandedCategory = false }
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat) },
                            onClick = {
                                category = cat
                                expandedCategory = false
                            }
                        )
                    }
                }
            }

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description *") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 6,
                shape = RoundedCornerShape(12.dp)
            )

            // Venue / Location
            OutlinedTextField(
                value = venue,
                onValueChange = { venue = it },
                label = { Text("Venue / Location *") },
                placeholder = { Text("e.g. Science City Auditorium, Bengaluru") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Date & Time
            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Date & Time *") },
                placeholder = { Text("e.g. 2026-11-15 18:00") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Price & Capacity
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price (₹ INR) *") },
                    placeholder = { Text("0 for Free") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = capacity,
                    onValueChange = { capacity = it },
                    label = { Text("Capacity *") },
                    placeholder = { Text("100") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Banner Image URL
            OutlinedTextField(
                value = bannerUrl,
                onValueChange = { bannerUrl = it },
                label = { Text("Banner Image URL") },
                placeholder = { Text("https://images.unsplash.com/...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            val parsedPrice = price.toDoubleOrNull() ?: 0.0
            val parsedCapacity = capacity.toIntOrNull() ?: 100
            val isFormValid = title.isNotBlank() && description.isNotBlank() &&
                    venue.isNotBlank() && date.isNotBlank() && parsedCapacity > 0

            // Submit Button
            Button(
                onClick = {
                    if (isFormValid) {
                        val eventPayload = Event(
                            id = eventId ?: "",
                            title = title.trim(),
                            description = description.trim(),
                            category = category,
                            date = date.trim(),
                            location = venue.trim(),
                            price = parsedPrice,
                            totalSeats = parsedCapacity,
                            availableSeats = parsedCapacity,
                            imageUrl = bannerUrl.trim(),
                            status = "PENDING_APPROVAL"
                        )
                        if (isEditMode) {
                            viewModel.updateEvent(eventId!!, eventPayload)
                        } else {
                            viewModel.createEvent(eventPayload)
                        }
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isFormValid,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (isEditMode) "Update & Submit for Review" else "Create & Submit for Review",
                    modifier = Modifier.padding(vertical = 4.dp),
                    fontWeight = FontWeight.Bold
                )
            }

            // Delete Button (Edit Mode only)
            if (isEditMode) {
                OutlinedButton(
                    onClick = { showDeleteConfirmDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Delete Event", modifier = Modifier.padding(vertical = 4.dp))
                }
            } else {
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel")
                }
            }
        }
    }
}
