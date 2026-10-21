package com.example.eventpulse.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.eventpulse.data.model.Role
import com.example.eventpulse.data.model.User
import com.example.eventpulse.ui.authViewModel
import com.example.eventpulse.ui.navigation.Screen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUsersScreen(
    navController: NavHostController,
    viewModel: AdminViewModel
) {
    val uiState by viewModel.adminUiState.collectAsStateWithLifecycle()
    val authVm = authViewModel()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var userToToggleBlock by remember { mutableStateOf<User?>(null) }
    var showLogoutConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadUsers()
    }

    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    // Confirmation Dialog for Block/Unblock
    if (userToToggleBlock != null) {
        val user = userToToggleBlock!!
        val willBlock = !user.isBlocked
        AlertDialog(
            onDismissRequest = { userToToggleBlock = null },
            title = {
                Text(if (willBlock) "Block User?" else "Unblock User?")
            },
            text = {
                Column {
                    Text(
                        if (willBlock)
                            "Are you sure you want to block ${user.name} (${user.email})? Blocked users will be immediately logged out and unable to log in."
                        else
                            "Are you sure you want to unblock ${user.name} (${user.email})? They will regain access to their account."
                    )
                }
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (willBlock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    ),
                    onClick = {
                        val userId = user.id
                        userToToggleBlock = null
                        viewModel.setUserBlockedStatus(userId, willBlock)
                    }
                ) {
                    Text(if (willBlock) "Block User" else "Unblock User")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { userToToggleBlock = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showLogoutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmDialog = false },
            title = { Text("Logout from Admin Portal?") },
            text = { Text("Are you sure you want to log out of the administration dashboard?") },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    onClick = {
                        showLogoutConfirmDialog = false
                        authVm.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                ) {
                    Text("Logout")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showLogoutConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User Moderation") },
                actions = {
                    IconButton(onClick = { viewModel.loadUsers() }) {
                        Text("🔄", style = MaterialTheme.typography.titleMedium)
                    }
                    IconButton(onClick = { showLogoutConfirmDialog = true }) {
                        Text("🚪", style = MaterialTheme.typography.titleMedium)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading && uiState.users.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.users.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("👥", style = MaterialTheme.typography.displayMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "No Users Found",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.users) { user ->
                            UserModerationCard(
                                user = user,
                                onToggleBlock = { userToToggleBlock = user }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UserModerationCard(
    user: User,
    onToggleBlock: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!user.phoneNumber.isNullOrBlank()) {
                        Text(
                            text = "📞 ${user.phoneNumber}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Role Pill
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when (user.role) {
                            Role.ADMIN -> MaterialTheme.colorScheme.errorContainer
                            Role.ORGANIZER -> MaterialTheme.colorScheme.secondaryContainer
                            else -> MaterialTheme.colorScheme.primaryContainer
                        }
                    ) {
                        Text(
                            text = user.role.name,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = when (user.role) {
                                Role.ADMIN -> MaterialTheme.colorScheme.error
                                Role.ORGANIZER -> MaterialTheme.colorScheme.secondary
                                else -> MaterialTheme.colorScheme.primary
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    // Status Pill
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (user.isBlocked) MaterialTheme.colorScheme.errorContainer else Color(0xFF10B981).copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = if (user.isBlocked) "BLOCKED" else "ACTIVE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (user.isBlocked) MaterialTheme.colorScheme.error else Color(0xFF10B981),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Don't show block button on Admin accounts
            if (user.role != Role.ADMIN) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (user.isBlocked) {
                        Button(
                            onClick = onToggleBlock,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF10B981)
                            )
                        ) {
                            Text("Unblock User")
                        }
                    } else {
                        OutlinedButton(
                            onClick = onToggleBlock,
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Block User")
                        }
                    }
                }
            }
        }
    }
}
