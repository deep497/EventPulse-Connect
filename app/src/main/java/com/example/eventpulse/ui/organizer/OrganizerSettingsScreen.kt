package com.example.eventpulse.ui.organizer

import androidx.compose.foundation.background
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
fun OrganizerSettingsScreen(
    navController: NavHostController,
    viewModel: OrganizerViewModel
) {
    val authVm = authViewModel()
    val authState by authVm.authUiState.collectAsStateWithLifecycle()
    val isDark = LocalThemeIsDark.current

    var showEditDialog by remember { mutableStateOf(false) }
    var editedName by remember(authState.fullName) {
        mutableStateOf(authState.fullName.ifBlank { authState.user?.name ?: "Organizer" })
    }

    val orgInitial = editedName.take(1).uppercase()
    val orgEmail = authState.email.ifBlank { authState.user?.email ?: "organizer@example.com" }

    // Edit Profile Dialog
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = {
                Text(
                    text = "Edit Organiser Profile",
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) OrgDarkTextPrimary else OrgLightTextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Update your organiser / brand name",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDark) OrgDarkTextSecondary else OrgLightTextSecondary
                    )
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        label = { Text("Organiser Name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isDark) RosePlum else RoyalPlum,
                            unfocusedBorderColor = if (isDark) OrgDarkBorder else PlumBorder
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
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) RosePlum else RoyalPlum,
                        contentColor = if (isDark) Color(0xFF171219) else Color.White
                    )
                ) {
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel", color = if (isDark) OrgDarkTextSecondary else OrgLightTextSecondary)
                }
            },
            containerColor = if (isDark) OrgDarkCard else Color.White,
            shape = RoundedCornerShape(18.dp)
        )
    }

    Scaffold(
        containerColor = if (isDark) OrgDarkBackgroundStart else OrgLightBackgroundStart,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Organiser Profile",
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) OrgDarkTextPrimary else OrgLightTextPrimary
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Organizer Profile Card
            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Royal Plum Avatar
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF382337) else RoyalPlum),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = orgInitial,
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
                        color = if (isDark) OrgDarkTextPrimary else OrgLightTextPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = orgEmail,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDark) OrgDarkTextSecondary else OrgLightTextSecondary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Role Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDark) Color(0x33DE748F) else PlumLightTint)
                            .padding(horizontal = 14.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Event Organiser",
                            color = if (isDark) RosePlum else RoyalPlum,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Account & Preferences Header
            Text(
                text = "Preferences & Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isDark) OrgDarkTextPrimary else OrgLightTextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Edit Profile Option
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
                        tint = if (isDark) RosePlum else RoyalPlum,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        "Edit Profile",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) OrgDarkTextPrimary else OrgLightTextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Notifications Option
            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_popup_reminder),
                        contentDescription = null,
                        tint = if (isDark) RosePlum else RoyalPlum,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        "Notifications",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) OrgDarkTextPrimary else OrgLightTextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Privacy & Security Option
            GlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_secure),
                        contentDescription = null,
                        tint = if (isDark) RosePlum else RoyalPlum,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        "Privacy & Security",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDark) OrgDarkTextPrimary else OrgLightTextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Logout Option
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

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
