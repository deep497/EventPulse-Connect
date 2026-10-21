package com.example.eventpulse.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventpulse.data.model.Event
import com.example.eventpulse.data.model.User
import com.example.eventpulse.data.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminUiState(
    val totalUsers: Int = 0,
    val totalEvents: Int = 0,
    val pendingEventsCount: Int = 0,
    val publishedEventsCount: Int = 0,
    val totalBookings: Int = 0,
    val totalRevenue: Double = 0.0,
    val pendingEvents: List<Event> = emptyList(),
    val users: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class AdminViewModel(
    private val eventRepository: EventRepository
) : ViewModel() {

    private val _adminUiState = MutableStateFlow(AdminUiState())
    val adminUiState: StateFlow<AdminUiState> = _adminUiState.asStateFlow()

    init {
        loadDashboardData()
    }

    // ── Dashboard ──────────────────────────────────────────────────────────────

    fun loadDashboardData() {
        viewModelScope.launch {
            _adminUiState.update { it.copy(isLoading = true, errorMessage = null) }

            // Load metrics and pending events concurrently via sequential calls
            // (Parallel could use async/await; sequential is simpler and fine here)
            val metricsResult = eventRepository.getAdminMetrics()
            val pendingResult = eventRepository.getPendingEvents()

            _adminUiState.update { state ->
                var updated = state.copy(isLoading = false)

                metricsResult.onSuccess { metrics ->
                    updated = updated.copy(
                        totalUsers = (metrics["totalUsers"] as? Double)?.toInt() ?: 0,
                        totalEvents = (metrics["totalEvents"] as? Double)?.toInt() ?: 0,
                        pendingEventsCount = (metrics["pendingEvents"] as? Double)?.toInt() ?: 0,
                        publishedEventsCount = (metrics["publishedEvents"] as? Double)?.toInt() ?: 0,
                        totalBookings = (metrics["totalBookings"] as? Double)?.toInt() ?: 0,
                        totalRevenue = (metrics["totalRevenue"] as? Double) ?: 0.0
                    )
                }.onFailure { err ->
                    updated = updated.copy(errorMessage = err.message)
                }

                pendingResult.onSuccess { events ->
                    updated = updated.copy(pendingEvents = events)
                }.onFailure { err ->
                    if (updated.errorMessage == null) updated = updated.copy(errorMessage = err.message)
                }

                updated
            }
        }
    }

    // ── Event moderation ───────────────────────────────────────────────────────

    /** Approves an event: calls `PATCH /api/admin/events/:id/status` with PUBLISHED. */
    fun approveEvent(eventId: String) {
        viewModelScope.launch {
            _adminUiState.update { it.copy(isLoading = true, errorMessage = null) }
            eventRepository.updateEventStatus(eventId, "PUBLISHED")
                .onSuccess {
                    _adminUiState.update { state ->
                        state.copy(
                            isLoading = false,
                            pendingEvents = state.pendingEvents.filter { it.id != eventId },
                            successMessage = "Event approved and published"
                        )
                    }
                }
                .onFailure { err ->
                    _adminUiState.update { it.copy(isLoading = false, errorMessage = err.message) }
                }
        }
    }

    /** Rejects an event: calls `PATCH /api/admin/events/:id/status` with REJECTED. */
    fun rejectEvent(eventId: String, @Suppress("UNUSED_PARAMETER") reason: String = "") {
        viewModelScope.launch {
            _adminUiState.update { it.copy(isLoading = true, errorMessage = null) }
            eventRepository.updateEventStatus(eventId, "REJECTED")
                .onSuccess {
                    _adminUiState.update { state ->
                        state.copy(
                            isLoading = false,
                            pendingEvents = state.pendingEvents.filter { it.id != eventId },
                            successMessage = "Event rejected"
                        )
                    }
                }
                .onFailure { err ->
                    _adminUiState.update { it.copy(isLoading = false, errorMessage = err.message) }
                }
        }
    }

    // ── User management ────────────────────────────────────────────────────────

    fun loadUsers() {
        viewModelScope.launch {
            _adminUiState.update { it.copy(isLoading = true, errorMessage = null) }
            eventRepository.getAdminUsers()
                .onSuccess { users ->
                    _adminUiState.update { it.copy(isLoading = false, users = users) }
                }
                .onFailure { err ->
                    _adminUiState.update { it.copy(isLoading = false, errorMessage = err.message) }
                }
        }
    }

    fun setUserBlockedStatus(userId: String, isBlocked: Boolean) {
        viewModelScope.launch {
            _adminUiState.update { it.copy(isLoading = true, errorMessage = null) }
            eventRepository.setUserBlockedStatus(userId, isBlocked)
                .onSuccess {
                    _adminUiState.update { state ->
                        val updatedUsers = state.users.map { u ->
                            if (u.id == userId) u.copy(isBlocked = isBlocked) else u
                        }
                        state.copy(
                            isLoading = false,
                            users = updatedUsers,
                            successMessage = if (isBlocked) "User blocked successfully" else "User unblocked successfully"
                        )
                    }
                }
                .onFailure { err ->
                    _adminUiState.update { it.copy(isLoading = false, errorMessage = err.message) }
                }
        }
    }

    fun suspendUser(userId: String) = setUserBlockedStatus(userId, true)

    // ── Helpers ────────────────────────────────────────────────────────────────

    fun clearMessages() {
        _adminUiState.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
