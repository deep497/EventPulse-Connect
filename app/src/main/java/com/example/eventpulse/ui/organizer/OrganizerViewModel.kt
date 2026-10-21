package com.example.eventpulse.ui.organizer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventpulse.data.model.Event
import com.example.eventpulse.data.repository.BookingRepository
import com.example.eventpulse.data.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class EventStatus {
    PUBLISHED,
    PENDING_APPROVAL,
    DRAFT
}

data class OrganizerUiState(
    val totalTicketSales: Int = 0,
    val totalRevenue: Double = 0.0,
    val managedEvents: List<Event> = emptyList(),
    val selectedEvent: Event? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class OrganizerViewModel(
    private val eventRepository: EventRepository,
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _organizerUiState = MutableStateFlow(OrganizerUiState())
    val organizerUiState: StateFlow<OrganizerUiState> = _organizerUiState.asStateFlow()

    init {
        loadOrganizerEvents()
    }

    fun loadOrganizerEvents() {
        viewModelScope.launch {
            _organizerUiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            val result = eventRepository.getEvents()
            result.fold(
                onSuccess = { events ->
                    // Calculate metrics from actual tickets sold (totalSeats - availableSeats)
                    val totalSales = events.sumOf { (it.totalSeats - it.availableSeats).coerceAtLeast(0) }
                    val revenue = events.sumOf { (it.totalSeats - it.availableSeats).coerceAtLeast(0) * it.price }
                    
                    _organizerUiState.update { it.copy(
                        isLoading = false,
                        managedEvents = events,
                        totalTicketSales = totalSales,
                        totalRevenue = revenue
                    )}
                },
                onFailure = { exception ->
                    _organizerUiState.update { it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Failed to load events"
                    )}
                }
            )
        }
    }

    fun loadManagedEvents() = loadOrganizerEvents()

    fun createEvent(event: Event) {
        viewModelScope.launch {
            _organizerUiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            val result = eventRepository.createEvent(event)
            result.fold(
                onSuccess = { createdEvent ->
                    _organizerUiState.update { it.copy(
                        isLoading = false,
                        managedEvents = it.managedEvents + createdEvent,
                        successMessage = "Event created successfully"
                    )}
                },
                onFailure = { exception ->
                    _organizerUiState.update { it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Failed to create event"
                    )}
                }
            )
        }
    }

    fun updateEvent(eventId: String, event: Event) {
        viewModelScope.launch {
            _organizerUiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            val result = eventRepository.updateEvent(eventId, event)
            result.fold(
                onSuccess = { updatedEvent ->
                    _organizerUiState.update { it.copy(
                        isLoading = false,
                        managedEvents = it.managedEvents.map { e -> 
                            if (e.id == eventId) updatedEvent else e 
                        },
                        successMessage = "Event updated successfully"
                    )}
                },
                onFailure = { exception ->
                    _organizerUiState.update { it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Failed to update event"
                    )}
                }
            )
        }
    }

    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            _organizerUiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            val result = eventRepository.deleteEvent(eventId)
            result.fold(
                onSuccess = {
                    _organizerUiState.update { it.copy(
                        isLoading = false,
                        managedEvents = it.managedEvents.filter { e -> e.id != eventId },
                        successMessage = "Event deleted successfully"
                    )}
                },
                onFailure = { exception ->
                    _organizerUiState.update { it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Failed to delete event"
                    )}
                }
            )
        }
    }

    /**
     * Performs check-in via POST /api/organizer/check-in.
     * @param qrCodeHash SHA-256 hash scanned from the attendee's ticket QR code.
     * @param bookingId Manual booking ID entered by the organizer.
     */
    fun checkIn(
        qrCodeHash: String? = null,
        bookingId: String? = null,
        onResult: ((Boolean, String) -> Unit)? = null
    ) {
        viewModelScope.launch {
            _organizerUiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = bookingRepository.checkIn(qrCodeHash, bookingId)
            result.fold(
                onSuccess = { res ->
                    val msg = (res["message"] as? String) ?: "Attendee checked in successfully"
                    _organizerUiState.update { it.copy(
                        isLoading = false,
                        successMessage = msg
                    )}
                    onResult?.invoke(true, msg)
                },
                onFailure = { exception ->
                    val err = exception.message ?: "Check-in failed"
                    _organizerUiState.update { it.copy(
                        isLoading = false,
                        errorMessage = err
                    )}
                    onResult?.invoke(false, err)
                }
            )
        }
    }

    /** Legacy alias kept for any call sites that still use the old name. */
    fun verifyBooking(qrCodeHash: String) = checkIn(qrCodeHash)

    fun clearMessages() {
        _organizerUiState.update { it.copy(
            errorMessage = null,
            successMessage = null
        )}
    }
}
