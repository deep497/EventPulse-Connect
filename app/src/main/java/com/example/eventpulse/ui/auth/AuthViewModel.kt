package com.example.eventpulse.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventpulse.data.model.Role
import com.example.eventpulse.data.model.User
import com.example.eventpulse.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val selectedRole: Role = Role.USER,
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val errorMessage: String? = null,
    val user: User? = null,
    val userRole: Role? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authUiState = MutableStateFlow(AuthUiState())
    val authUiState: StateFlow<AuthUiState> = _authUiState.asStateFlow()

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        viewModelScope.launch {
            authRepository.getJwtToken().collect { token ->
                if (token != null) {
                    // Get user role from session
                    authRepository.getUserRole().collect { role ->
                        _authUiState.update { it.copy(
                            isAuthenticated = true,
                            userRole = role
                        )}
                    }
                } else {
                    _authUiState.update { it.copy(
                        isAuthenticated = false,
                        userRole = null
                    )}
                }
            }
        }
    }

    fun updateFullName(name: String) {
        _authUiState.update { it.copy(fullName = name) }
    }

    fun updateEmail(email: String) {
        _authUiState.update { it.copy(email = email) }
    }

    fun updatePassword(password: String) {
        _authUiState.update { it.copy(password = password) }
    }

    fun updateSelectedRole(role: Role) {
        _authUiState.update { it.copy(selectedRole = role) }
    }

    fun login() {
        val email = _authUiState.value.email
        val password = _authUiState.value.password

        if (email.isBlank() || password.isBlank()) {
            _authUiState.update { it.copy(errorMessage = "Email and password are required") }
            return
        }

        viewModelScope.launch {
            _authUiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            val result = authRepository.login(email, password)
            result.fold(
                onSuccess = { user ->
                    _authUiState.update { it.copy(
                        isLoading = false,
                        user = user,
                        userRole = user.role,
                        isAuthenticated = true,
                        errorMessage = null
                    )}
                },
                onFailure = { exception ->
                    _authUiState.update { it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Login failed"
                    )}
                }
            )
        }
    }

    fun signup() {
        val name = _authUiState.value.fullName
        val email = _authUiState.value.email
        val password = _authUiState.value.password
        val role = _authUiState.value.selectedRole

        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _authUiState.update { it.copy(errorMessage = "All fields are required") }
            return
        }

        viewModelScope.launch {
            _authUiState.update { it.copy(isLoading = true, errorMessage = null) }
            
            val result = authRepository.register(name, email, password, role.name)
            result.fold(
                onSuccess = { user ->
                    // After successful signup, automatically login
                    login()
                },
                onFailure = { exception ->
                    _authUiState.update { it.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Signup failed"
                    )}
                }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _authUiState.value = AuthUiState()
        }
    }

    fun clearError() {
        _authUiState.update { it.copy(errorMessage = null) }
    }

    fun clearFormFields() {
        _authUiState.update { it.copy(
            fullName = "",
            email = "",
            password = "",
            errorMessage = null
        )}
    }
}
