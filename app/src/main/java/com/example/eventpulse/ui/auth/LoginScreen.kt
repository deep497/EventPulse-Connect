package com.example.eventpulse.ui.auth

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.draw.shadow
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
fun LoginScreen(
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
                popUpTo(Screen.Login.route) { inclusive = true }
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
            // Header with glowing pulse waveform
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "EventPulse",
                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 36.sp),
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDark) NeonCyan else ElectricPurple
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Waveform pulse badge
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0x3300F5D4) else Color(0x227B2CBF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_btn_speak_now),
                        contentDescription = "Pulse Waveform",
                        tint = if (isDark) NeonCyan else ElectricPurple,
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

            Spacer(modifier = Modifier.height(36.dp))

            // Glassmorphic Card Container
            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(22.dp)
                ) {
                    Text(
                        text = "Welcome back!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) DarkTextPrimary else LightTextPrimary
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Role Selector
                    Text(
                        text = "Role Selector",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDark) DarkTextSecondary else LightTextSecondary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    NeonRoleSelector(
                        selectedRole = uiState.selectedRole,
                        onRoleSelected = { viewModel.updateSelectedRole(it) }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Email Field
                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = { viewModel.updateEmail(it) },
                        label = { Text("Email") },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_dialog_email),
                                contentDescription = null,
                                tint = if (isDark) NeonCyan else ElectricPurple
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isDark) NeonCyan else ElectricPurple,
                            unfocusedBorderColor = if (isDark) DarkGlassBorderStart else LightGlassBorder,
                            focusedContainerColor = if (isDark) DarkSearchField else LightSearchField,
                            unfocusedContainerColor = if (isDark) DarkSearchField else LightSearchField
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !uiState.isLoading
                    )

                    Spacer(modifier = Modifier.height(16.dp))

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
                                tint = if (isDark) NeonCyan else ElectricPurple
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
                            focusedBorderColor = if (isDark) NeonCyan else ElectricPurple,
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

                    // Forgot Password
                    TextButton(
                        onClick = { /* TODO */ },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(
                            text = "Forgot Password?",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDark) CyberBlue else ElectricPurple
                        )
                    }

                    // Error Message
                    if (uiState.errorMessage != null) {
                        Text(
                            text = uiState.errorMessage!!,
                            color = StatusRejected,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Glowing Purple-to-Cyan "Continue" Button
                    GradientButton(
                        text = "Continue",
                        onClick = { viewModel.login() },
                        isLoading = uiState.isLoading,
                        enabled = !uiState.isLoading,
                        gradient = if (isDark) PurpleCyanGradient else Brush.horizontalGradient(listOf(ElectricPurple, CyberBlue)),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = if (isDark) Color(0x33B3A8D6) else Color(0x336B5C8E)
                        )
                        Text(
                            text = "  or  ",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isDark) DarkTextTertiary else LightTextTertiary
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = if (isDark) Color(0x33B3A8D6) else Color(0x336B5C8E)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Dark Translucent "Continue with Google"
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
                            containerColor = if (isDark) Color(0x401C1538) else Color(0x80FFFFFF)
                        ),
                        enabled = !uiState.isLoading
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_dialog_info),
                                contentDescription = "Google",
                                tint = if (isDark) SunsetGold else ElectricPurple,
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

            // Toggle to Signup
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Don't have an account?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isDark) DarkTextSecondary else LightTextSecondary
                )
                TextButton(
                    onClick = {
                        viewModel.clearFormFields()
                        navController.navigate(Screen.Signup.route)
                    }
                ) {
                    Text(
                        text = "Sign up",
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) NeonCyan else ElectricPurple
                    )
                }
            }
        }
    }
}

/**
 * 3-Tab Role Selector pill bar with glowing active tab
 */
@Composable
fun NeonRoleSelector(
    selectedRole: Role,
    onRoleSelected: (Role) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalThemeIsDark.current
    val containerColor = if (isDark) Color(0x80100A24) else Color(0x66DDD6FE)
    val borderColor = if (isDark) DarkGlassBorderStart else LightGlassBorder

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(containerColor)
            .border(1.dp, borderColor, RoundedCornerShape(24.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Role.values().forEach { role ->
            val isSelected = selectedRole == role
            val activeColor by animateColorAsState(
                targetValue = if (isSelected) {
                    if (isDark) CyberBlue else ElectricPurple
                } else {
                    Color.Transparent
                },
                label = "rolePill"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(20.dp))
                    .background(activeColor)
                    .then(
                        if (isSelected && isDark) {
                            Modifier.shadow(8.dp, RoundedCornerShape(20.dp), spotColor = CyberBlue)
                        } else Modifier
                    )
                    .clickable { onRoleSelected(role) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = role.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color.White else if (isDark) DarkTextSecondary else LightTextSecondary
                )
            }
        }
    }
}
