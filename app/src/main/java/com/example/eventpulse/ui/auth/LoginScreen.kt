package com.example.eventpulse.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.eventpulse.data.model.Role
import com.example.eventpulse.ui.navigation.Screen

@Composable
fun LoginScreen(
    navController: NavHostController,
    viewModel: AuthViewModel
) {
    val uiState by viewModel.authUiState.collectAsStateWithLifecycle()

    // Navigate to appropriate home screen when authenticated
    LaunchedEffect(uiState.isAuthenticated, uiState.userRole) {
        if (uiState.isAuthenticated && uiState.userRole != null) {
            val destination = when (uiState.userRole) {
                Role.USER -> Screen.UserHome.route
                Role.ORGANIZER -> Screen.OrganizerDashboard.route
                Role.ADMIN -> Screen.AdminDashboard.route
                else -> Screen.UserHome.route
            }
            navController.navigate(destination) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }

    // Show error message
    LaunchedEffect(uiState.errorMessage) {
        if (uiState.errorMessage != null) {
            // Error will be shown in UI
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Header
        Text(
            text = "EventPulse",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = "Personalized event recommendations",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Welcome Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Welcome back!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Role Selector
                Text(
                    text = "Select your role",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                RoleSelector(
                    selectedRole = uiState.selectedRole,
                    onRoleSelected = { viewModel.updateSelectedRole(it) }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Email Field
                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = { viewModel.updateEmail(it) },
                    label = { Text("Email") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !uiState.isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password Field
                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = { viewModel.updatePassword(it) },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !uiState.isLoading
                )

                // Forgot Password
                TextButton(
                    onClick = { /* TODO: Navigate to forgot password */ },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Forgot Password?")
                }

                // Error Message
                if (uiState.errorMessage != null) {
                    Text(
                        text = uiState.errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Continue Button
                Button(
                    onClick = { viewModel.login() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Continue")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Continue with Google (Secondary Button)
                OutlinedButton(
                    onClick = { /* TODO: Google Sign-In */ },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading
                ) {
                    Text("Continue with Google")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Toggle to Signup
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Don't have an account?",
                style = MaterialTheme.typography.bodyMedium
            )
            TextButton(
                onClick = {
                    viewModel.clearFormFields()
                    navController.navigate(Screen.Signup.route)
                }
            ) {
                Text("Sign up")
            }
        }
    }
}

@Composable
fun RoleSelector(
    selectedRole: Role,
    onRoleSelected: (Role) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Role.values().forEach { role ->
            val isSelected = selectedRole == role
            FilterChip(
                selected = isSelected,
                onClick = { onRoleSelected(role) },
                label = {
                    Text(
                        text = role.name.lowercase().replaceFirstChar { it.uppercase() },
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}
