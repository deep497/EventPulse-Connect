package com.example.eventpulse.ui.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventpulse.data.local.SessionManager
import com.example.eventpulse.data.model.Booking
import com.example.eventpulse.data.model.Event
import com.example.eventpulse.data.repository.BookingRepository
import com.example.eventpulse.data.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UserUiState(
    val locationText: String = "Current Location",
    val searchQuery: String = "",
    val selectedCategory: String = "All",
    val categories: List<String> = listOf("All", "Music", "Tech", "Art", "Sports", "Spirituality"),
    val recommendedEvents: List<Event> = emptyList(),
    val favoriteEventIds: Set<String> = emptySet(),
    val selectedEvent: Event? = null,
    val bookings: List<Booking> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class UserViewModel(
    private val eventRepository: EventRepository,
    private val bookingRepository: BookingRepository,
    private val sessionManager: SessionManager? = null
) : ViewModel() {

    private val _userUiState = MutableStateFlow(UserUiState())
    val userUiState: StateFlow<UserUiState> = _userUiState.asStateFlow()

    init {
        loadEvents()
        loadFavorites()
    }

    private fun loadFavorites() {
        if (sessionManager != null) {
            viewModelScope.launch {
                sessionManager.getFavoriteEventIds().collect { ids ->
                    _userUiState.update { it.copy(favoriteEventIds = ids) }
                }
            }
        }
    }

    fun toggleFavorite(eventId: String) {
        val currentIds = _userUiState.value.favoriteEventIds
        val updated = if (currentIds.contains(eventId)) {
            currentIds - eventId
        } else {
            currentIds + eventId
        }
        _userUiState.update { it.copy(favoriteEventIds = updated) }
        if (sessionManager != null) {
            viewModelScope.launch {
                sessionManager.saveFavoriteEventIds(updated)
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _userUiState.update { it.copy(searchQuery = query) }
        val cat = _userUiState.value.selectedCategory.let { if (it == "All") null else it }
        val search = if (query.isBlank()) null else query.trim()
        loadEvents(category = cat, search = search)
    }

    fun updateSelectedCategory(category: String) {
        _userUiState.update { it.copy(selectedCategory = category) }
        val cat = if (category == "All") null else category
        val search = _userUiState.value.searchQuery.let { if (it.isBlank()) null else it.trim() }
        loadEvents(category = cat, search = search)
    }

    fun updateLocation(location: String) {
        _userUiState.update { it.copy(locationText = location) }
    }

    fun loadEvents(category: String? = null, search: String? = null) {
        viewModelScope.launch {
            _userUiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            val result = eventRepository.getEvents(status = "PUBLISHED", category = category, search = search)
            result.fold(
                onSuccess = { events ->
                    val activeEvents = events.filter { !it.isExpired }
                    _userUiState.update { it.copy(
                        isLoading = false,
                        recommendedEvents = activeEvents
                    )}
                },
                onFailure = { exception ->
                    _userUiState.update { it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Failed to load events"
                    )}
                }
            )
        }
    }

    fun loadEventById(eventId: String) {
        viewModelScope.launch {
            _userUiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            val result = eventRepository.getEventById(eventId)
            result.fold(
                onSuccess = { event ->
                    _userUiState.update { it.copy(
                        isLoading = false,
                        selectedEvent = event
                    )}
                },
                onFailure = { exception ->
                    _userUiState.update { it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Failed to load event"
                    )}
                }
            )
        }
    }

    fun loadBookings() {
        viewModelScope.launch {
            _userUiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = bookingRepository.getMyTickets()
            result.fold(
                onSuccess = { bookings ->
                    _userUiState.update { it.copy(
                        isLoading = false,
                        bookings = bookings
                    )}
                },
                onFailure = { exception ->
                    _userUiState.update { it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Failed to load bookings"
                    )}
                }
            )
        }
    }

    fun cancelBooking(bookingId: String, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _userUiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = bookingRepository.cancelBooking(bookingId)
            result.fold(
                onSuccess = { response ->
                    val message = (response["message"] as? String) ?: "Booking cancelled successfully"
                    // Refresh bookings list
                    loadBookings()
                    onComplete(true, message)
                },
                onFailure = { exception ->
                    _userUiState.update { it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Failed to cancel booking"
                    )}
                    onComplete(false, exception.message ?: "Cancellation failed")
                }
            )
        }
    }

    fun createBooking(eventId: String, ticketCount: Int) {
        viewModelScope.launch {
            _userUiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            val result = bookingRepository.createBooking(eventId, ticketCount)
            result.fold(
                onSuccess = { booking ->
                    _userUiState.update { it.copy(
                        isLoading = false,
                        bookings = it.bookings + booking
                    )}
                },
                onFailure = { exception ->
                    _userUiState.update { it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Failed to create booking"
                    )}
                }
            )
        }
    }

    fun clearError() {
        _userUiState.update { it.copy(errorMessage = null) }
    }
}
