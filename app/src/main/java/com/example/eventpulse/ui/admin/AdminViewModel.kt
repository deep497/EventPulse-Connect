package com.example.eventpulse.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventpulse.data.model.Event
import com.example.eventpulse.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminUiState(
    val totalUsers: Int = 0,
    val totalEvents: Int = 0,
    val totalRevenue: Double = 0.0,
    val pendingEvents: List<Event> = emptyList(),
    val users: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class AdminViewModel : ViewModel() {

    private val _adminUiState = MutableStateFlow(AdminUiState())
    val adminUiState: StateFlow<AdminUiState> = _adminUiState.asStateFlow()

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _adminUiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            // Placeholder for loading admin data
            // In a real implementation, this would call repositories to fetch:
            // - All users
            // - All events
            // - Platform statistics
            // - Pending events for moderation
            
            _adminUiState.update { it.copy(
                isLoading = false,
                totalUsers = 150,
                totalEvents = 45,
                totalRevenue = 12500.00,
                pendingEvents = emptyList(),
                users = emptyList()
            )}
        }
    }

    fun approveEvent(eventId: String) {
        viewModelScope.launch {
            _adminUiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            // TODO: Implement API call to approve event
            
            _adminUiState.update { it.copy(
                isLoading = false,
                pendingEvents = it.pendingEvents.filter { event -> event.id != eventId },
                successMessage = "Event approved successfully"
            )}
        }
    }

    fun rejectEvent(eventId: String, reason: String) {
        viewModelScope.launch {
            _adminUiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            // TODO: Implement API call to reject event
            
            _adminUiState.update { it.copy(
                isLoading = false,
                pendingEvents = it.pendingEvents.filter { event -> event.id != eventId },
                successMessage = "Event rejected"
            )}
        }
    }

    fun suspendUser(userId: String) {
        viewModelScope.launch {
            _adminUiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            // TODO: Implement API call to suspend user
            
            _adminUiState.update { it.copy(
                isLoading = false,
                successMessage = "User suspended"
            )}
        }
    }

    fun clearMessages() {
        _adminUiState.update { it.copy(
            errorMessage = null,
            successMessage = null
        )}
    }
}
