package com.example.eventpulse.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.eventpulse.ui.authViewModel
import com.example.eventpulse.ui.components.DarkLightModeToggle
import com.example.eventpulse.ui.components.GlassCard
import com.example.eventpulse.ui.navigation.Screen
import com.example.eventpulse.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    navController: NavHostController,
    viewModel: UserViewModel
) {
    val authVm = authViewModel()
    val authState by authVm.authUiState.collectAsStateWithLifecycle()
    val isDark = LocalThemeIsDark.current

    var showEditDialog by remember { mutableStateOf(false) }
    var editedName by remember(authState.fullName) {
        mutableStateOf(authState.fullName.ifBlank { authState.user?.name ?: "Alex" })
    }

    val userInitial = editedName.take(1).uppercase()
    val userEmail = authState.email.ifBlank { authState.user?.email ?: "user@example.com" }

    // Edit Profile Dialog
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = {
                Text(
                    text = "Edit Profile",
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) UserDarkTextPrimary else UserLightTextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Update your display name",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDark) UserDarkTextSecondary else UserLightTextSecondary
                    )
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        label = { Text("Full Name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CoralPrimary,
                            unfocusedBorderColor = if (isDark) UserDarkBorder else UserLightBorder
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editedName.isNotBlank()) {
                            authVm.updateProfile(editedName.trim())
                        }
                        showEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralPrimary)
                ) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel", color = if (isDark) UserDarkTextSecondary else UserLightTextSecondary)
                }
            },
            containerColor = if (isDark) UserDarkCard else Color.White,
            shape = RoundedCornerShape(18.dp)
        )
    }

    Scaffold(
        containerColor = if (isDark) UserDarkBackgroundStart else UserLightBackgroundStart,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "My Profile",
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) UserDarkTextPrimary else UserLightTextPrimary
                    )
                },
                actions = {
                    DarkLightModeToggle()
                    Spacer(modifier = Modifier.width(16.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Profile Header Card
            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar circle in solid Sunset Coral
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(CoralPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userInitial,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 34.sp,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = editedName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) UserDarkTextPrimary else UserLightTextPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = userEmail,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDark) UserDarkTextSecondary else UserLightTextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Role Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(CoralPrimary.copy(alpha = 0.15f))
                            .padding(horizontal = 14.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Attendee",
                            color = CoralPrimary,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Account Settings
            Text(
                text = "Account Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isDark) UserDarkTextPrimary else UserLightTextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Edit Profile Button
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showEditDialog = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_edit),
                        contentDescription = null,
                        tint = CoralPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        "Edit Profile",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) UserDarkTextPrimary else UserLightTextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Logout Button
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        authVm.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_lock_power_off),
                        contentDescription = null,
                        tint = StatusRejected,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        "Logout",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = StatusRejected
                    )
                }
            }
        }
    }
}
