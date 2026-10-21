package com.example.eventpulse.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.eventpulse.data.model.Role
import com.example.eventpulse.ui.components.AppBackground
import com.example.eventpulse.ui.components.DarkLightModeToggle
import com.example.eventpulse.ui.components.GlassCard
import com.example.eventpulse.ui.components.GradientButton
import com.example.eventpulse.ui.navigation.Screen
import com.example.eventpulse.ui.theme.*

@Composable
fun SignupScreen(
    navController: NavHostController,
    viewModel: AuthViewModel
) {
    val uiState by viewModel.authUiState.collectAsStateWithLifecycle()
    val isDark = LocalThemeIsDark.current

    // Navigate to appropriate home screen when authenticated
    LaunchedEffect(uiState.isAuthenticated, uiState.userRole) {
        if (uiState.isAuthenticated && uiState.userRole != null) {
            val destination = when (uiState.userRole) {
                Role.USER -> Screen.UserExplore.route
                Role.ORGANIZER -> Screen.OrgDashboard.route
                Role.ADMIN -> Screen.AdminModeration.route
                else -> Screen.UserExplore.route
            }
            navController.navigate(destination) {
                popUpTo(Screen.Signup.route) { inclusive = true }
            }
        }
    }

    AppBackground {
        // Top theme toggle button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 44.dp, end = 20.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            DarkLightModeToggle()
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 70.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Header with waveform
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "EventPulse",
                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 36.sp),
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDark) CoralSoft else CoralPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0x33FF6B4A) else Color(0x22E03E2D)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_btn_speak_now),
                        contentDescription = "Pulse Waveform",
                        tint = if (isDark) CoralSoft else CoralPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Text(
                text = "Personalized event recommendations",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isDark) DarkTextSecondary else LightTextSecondary,
                modifier = Modifier.padding(top = 6.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Container Card
            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(22.dp)
                ) {
                    Text(
                        text = "Create Account",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) DarkTextPrimary else LightTextPrimary
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Role Selector
                    Text(
                        text = "I want to join as",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDark) DarkTextSecondary else LightTextSecondary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    NeonRoleSelector(
                        selectedRole = if (uiState.selectedRole == Role.ADMIN) Role.USER else uiState.selectedRole,
                        onRoleSelected = { viewModel.updateSelectedRole(it) },
                        roles = listOf(Role.USER, Role.ORGANIZER)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Full Name Field
                    OutlinedTextField(
                        value = uiState.fullName,
                        onValueChange = { viewModel.updateFullName(it) },
                        label = { Text("Full Name") },
                        leadingIcon = { 
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_menu_myplaces),
                                contentDescription = null,
                                tint = if (isDark) CoralSoft else CoralPrimary
                            ) 
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CoralPrimary,
                            unfocusedBorderColor = if (isDark) DarkGlassBorderStart else LightGlassBorder,
                            focusedContainerColor = if (isDark) DarkSearchField else LightSearchField,
                            unfocusedContainerColor = if (isDark) DarkSearchField else LightSearchField
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !uiState.isLoading
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Email Field
                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = { viewModel.updateEmail(it) },
                        label = { Text("Email") },
                        leadingIcon = { 
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_dialog_email),
                                contentDescription = null,
                                tint = if (isDark) CoralSoft else CoralPrimary
                            ) 
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CoralPrimary,
                            unfocusedBorderColor = if (isDark) DarkGlassBorderStart else LightGlassBorder,
                            focusedContainerColor = if (isDark) DarkSearchField else LightSearchField,
                            unfocusedContainerColor = if (isDark) DarkSearchField else LightSearchField
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !uiState.isLoading
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Password Field with Eye Toggle
                    var passwordVisible by remember { mutableStateOf(false) }

                    OutlinedTextField(
                        value = uiState.password,
                        onValueChange = { viewModel.updatePassword(it) },
                        label = { Text("Password") },
                        leadingIcon = { 
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_secure),
                                contentDescription = null,
                                tint = if (isDark) CoralSoft else CoralPrimary
                            ) 
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    painter = painterResource(
                                        id = if (passwordVisible) android.R.drawable.ic_menu_view 
                                             else android.R.drawable.ic_secure
                                    ),
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                    tint = if (isDark) DarkTextSecondary else LightTextSecondary
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CoralPrimary,
                            unfocusedBorderColor = if (isDark) DarkGlassBorderStart else LightGlassBorder,
                            focusedContainerColor = if (isDark) DarkSearchField else LightSearchField,
                            unfocusedContainerColor = if (isDark) DarkSearchField else LightSearchField
                        ),
                        shape = RoundedCornerShape(14.dp),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !uiState.isLoading
                    )

                    // Error Message
                    if (uiState.errorMessage != null) {
                        Text(
                            text = uiState.errorMessage!!,
                            color = StatusRejected,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Sign Up Gradient Button
                    GradientButton(
                        text = "Sign Up",
                        onClick = { viewModel.signup() },
                        isLoading = uiState.isLoading,
                        enabled = !uiState.isLoading,
                        gradient = if (isDark) PlumRoseGradient else SunsetCoralGradient,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Continue with Google
                    OutlinedButton(
                        onClick = { /* TODO: Google Sign-In */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isDark) DarkGlassBorderStart else LightGlassBorder
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isDark) Color(0x40251B27) else Color(0x80FFFFFF)
                        ),
                        enabled = !uiState.isLoading
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_dialog_info),
                                contentDescription = "Google",
                                tint = if (isDark) SunsetGold else CoralPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Continue with Google",
                                color = if (isDark) DarkTextPrimary else LightTextPrimary,
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Toggle to Login
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isDark) DarkTextSecondary else LightTextSecondary
                )
                TextButton(
                    onClick = {
                        viewModel.clearFormFields()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Signup.route) { inclusive = true }
                        }
                    }
                ) {
                    Text(
                        text = "Log In",
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) CoralSoft else CoralPrimary
                    )
                }
            }
        }
    }
}
