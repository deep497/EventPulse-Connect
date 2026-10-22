package com.example.eventpulse.ui.user

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.eventpulse.data.model.Event
import com.example.eventpulse.ui.authViewModel
import com.example.eventpulse.ui.chat.AiChatBottomSheet
import com.example.eventpulse.ui.chatViewModel
import com.example.eventpulse.ui.components.DarkLightModeToggle
import com.example.eventpulse.ui.components.GlassCard
import com.example.eventpulse.ui.navigation.Screen
import com.example.eventpulse.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserHomeScreen(
    navController: NavHostController,
    viewModel: UserViewModel
) {
    val uiState by viewModel.userUiState.collectAsStateWithLifecycle()
    val authVm = authViewModel()
    val authState by authVm.authUiState.collectAsStateWithLifecycle()
    var showAiChat by remember { mutableStateOf(false) }
    val chatVm = chatViewModel()
    val isDark = LocalThemeIsDark.current

    val userName = authState.fullName.ifBlank { authState.user?.name ?: "Alex" }.split(" ").first()
    val userInitial = userName.take(1).uppercase()

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            // Circular Coral FAB with sparkle (Exact match to Sunset Coral User column)
            Box(
                modifier = Modifier
                    .padding(bottom = 12.dp)
                    .size(58.dp)
                    .shadow(12.dp, CircleShape, spotColor = if (isDark) AmberGold else CoralPrimary)
                    .clip(CircleShape)
                    .background(if (isDark) Brush.verticalGradient(listOf(AmberGold, CoralPrimary)) else Brush.verticalGradient(listOf(CoralPrimary, CoralAccent)))
                    .clickable { showAiChat = true },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "✦",
                    fontSize = 26.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { paddingValues ->
        if (showAiChat) {
            AiChatBottomSheet(
                role = "USER",
                onDismiss = { showAiChat = false },
                viewModel = chatVm
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDark) UserDarkBackgroundStart else UserLightBackgroundStart)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top Header Row: Hello, Alex (left) + Location Pill, Toggle & Coral Avatar (right)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left: Greeting
                        Column {
                            Text(
                                text = "Hello,",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isDark) UserDarkTextSecondary else UserLightTextSecondary
                            )
                            Text(
                                text = userName,
                                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp),
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isDark) UserDarkTextPrimary else UserLightTextPrimary
                            )
                        }

                        // Right: Location pill, Theme Switcher & Coral Avatar
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DarkLightModeToggle()

                            // Location Pill [Mumbai]
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isDark) UserDarkCard else Color.White)
                                    .border(1.dp, if (isDark) UserDarkBorder else UserLightBorder, RoundedCornerShape(20.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = uiState.locationText.ifBlank { "Mumbai" },
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isDark) UserDarkTextPrimary else UserLightTextPrimary
                                )
                            }

                            // User Avatar in Solid Coral
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (isDark) CoralAccent else CoralPrimary)
                                    .clickable { navController.navigate(Screen.UserProfile.route) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = userInitial,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Search Bar
                item {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                "Search events, artists, venues",
                                color = if (isDark) UserDarkTextTertiary else UserLightTextTertiary,
                                fontSize = 14.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_menu_search),
                                contentDescription = null,
                                tint = if (isDark) UserDarkTextSecondary else UserLightTextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CoralPrimary,
                            unfocusedBorderColor = if (isDark) UserDarkBorder else UserLightBorder,
                            focusedContainerColor = if (isDark) UserDarkSearchField else UserLightSearchField,
                            unfocusedContainerColor = if (isDark) UserDarkSearchField else UserLightSearchField
                        ),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = true
                    )
                }

                // Category Filter Chips: All (active in Coral), Music, Tech, Art, Sports
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val categories = listOf("All", "Music", "Tech", "Art", "Sports")

                        items(categories) { categoryName ->
                            val isSelected = uiState.selectedCategory.equals(categoryName, ignoreCase = true) ||
                                    (categoryName == "All" && uiState.selectedCategory.isEmpty())

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (isSelected) CoralPrimary
                                        else if (isDark) UserDarkCard else Color.White
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) CoralPrimary else if (isDark) UserDarkBorder else UserLightBorder,
                                        RoundedCornerShape(20.dp)
                                    )
                                    .clickable { viewModel.updateSelectedCategory(categoryName) }
                                    .padding(horizontal = 18.dp, vertical = 9.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = categoryName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else (if (isDark) UserDarkTextSecondary else UserLightTextSecondary)
                                )
                            }
                        }
                    }
                }

                // Section Header: "Recommended for you" + "See all"
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recommended for you",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) UserDarkTextPrimary else UserLightTextPrimary
                        )
                        Text(
                            text = "See all",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) CoralSoft else CoralPrimary
                        )
                    }
                }

                // Loading State
                if (uiState.isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = CoralPrimary)
                        }
                    }
                }

                // Error State
                if (uiState.errorMessage != null) {
                    item {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = uiState.errorMessage!!,
                                modifier = Modifier.padding(16.dp),
                                color = StatusRejected,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }

                // Recommended Events List
                items(uiState.recommendedEvents) { event ->
                    NeonEventCard(
                        event = event,
                        isFavorite = uiState.favoriteEventIds.contains(event.id),
                        onToggleFavorite = { viewModel.toggleFavorite(event.id) },
                        onClick = {
                            navController.navigate(Screen.EventDetail.createRoute(event.id))
                        }
                    )
                }

                // Empty State
                if (!uiState.isLoading && uiState.recommendedEvents.isEmpty()) {
                    item {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No Events Found",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) UserDarkTextPrimary else UserLightTextPrimary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Check back later or ask the AI Assistant for recommendations!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isDark) UserDarkTextSecondary else UserLightTextSecondary
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(60.dp))
                }
            }
        }
    }
}

/**
 * Rich Event Card matching Sunset Coral Reference Picture
 */
@Composable
fun NeonEventCard(
    event: Event,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isFavorite: Boolean = false,
    onToggleFavorite: () -> Unit = {}
) {
    val isDark = LocalThemeIsDark.current

    // Warm Sunset Coral banner gradient
    val bannerGradient = if (isDark) SunsetDarkHeroGradient else SunsetHeroGradient

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column {
            // Rounded Banner Image area with overlaid Price Pill
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(bannerGradient)
            ) {
                // Heart Favorite button on top-right
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xCC000000))
                ) {
                    Text(
                        text = if (isFavorite) "❤️" else "🤍",
                        fontSize = 18.sp
                    )
                }

                // Dark pill on bottom-right with price: e.g. ₹499+ or Free
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xE614110F))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (event.price > 0) "₹${event.price.toInt()}+" else "Free",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Category pill on top-left
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x99000000))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = event.category.ifBlank { "Event" },
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Event Details
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) UserDarkTextPrimary else UserLightTextPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Date & Venue row
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_today),
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = if (isDark) UserDarkTextSecondary else UserLightTextSecondary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = event.date.ifBlank { "Upcoming Date" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDark) UserDarkTextSecondary else UserLightTextSecondary
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_mylocation),
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = if (isDark) UserDarkTextSecondary else UserLightTextSecondary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = event.location.ifBlank { "Skyline Arena" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDark) UserDarkTextSecondary else UserLightTextSecondary
                    )
                }

                if (event.availableSeats > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "🔥 ${event.availableSeats} seats left",
                        style = MaterialTheme.typography.labelSmall,
                        color = CoralPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
