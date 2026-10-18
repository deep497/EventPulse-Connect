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

    val userName = authState.fullName.ifBlank { "Alex" }.split(" ").first()

    Scaffold(
        containerColor = Color.Transparent,
        floatingActionButton = {
            // Glowing Circular AI Chatbot FAB (Exact match to Reference Picture 1)
            Box(
                modifier = Modifier
                    .padding(bottom = 12.dp)
                    .size(62.dp)
                    .shadow(16.dp, CircleShape, spotColor = NeonCyan)
                    .clip(CircleShape)
                    .background(AiButtonGradient)
                    .border(2.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                    .clickable { showAiChat = true },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_dialog_info),
                    contentDescription = "AI Assistant",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
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
                .background(if (isDark) DarkBackgroundGradient else LightBackgroundGradient)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Top Location Bar + Dark/Light Toggle + Avatar
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Location Picker
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isDark) Color(0x401C1538) else Color(0x66DDD6FE))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_menu_mylocation),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (isDark) NeonCyan else ElectricPurple
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = uiState.locationText.ifBlank { "San Francisco, CA" },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isDark) DarkTextPrimary else LightTextPrimary
                            )
                        }

                        // Right actions: Theme Switcher & Profile Avatar
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            DarkLightModeToggle()

                            // Profile Avatar with neon ring
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (isDark) Color(0xFF261C4C) else Color(0xFFDDD6FE))
                                    .border(1.5.dp, if (isDark) NeonCyan else ElectricPurple, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = userName.take(1).uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = if (isDark) NeonCyan else ElectricPurple
                                )
                            }
                        }
                    }
                }

                // Greeting Header: "Hello, Alex!"
                item {
                    Column(modifier = Modifier.padding(top = 4.dp)) {
                        Text(
                            text = "Hello, $userName!",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isDark) DarkTextPrimary else LightTextPrimary
                        )
                    }
                }

                // Modern Search Bar
                item {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                "Search events, artists, venues...",
                                color = if (isDark) DarkTextTertiary else LightTextTertiary
                            )
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = android.R.drawable.ic_menu_search),
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
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true
                    )
                }

                // Category Filter Chips with Icons
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val categoriesWithIcons = listOf(
                            Pair("All", android.R.drawable.ic_menu_view),
                            Pair("Music", android.R.drawable.ic_media_play),
                            Pair("Tech", android.R.drawable.ic_menu_info_details),
                            Pair("Art", android.R.drawable.ic_menu_gallery),
                            Pair("Sports", android.R.drawable.ic_menu_compass)
                        )

                        items(categoriesWithIcons) { (categoryName, iconRes) ->
                            val isSelected = uiState.selectedCategory.equals(categoryName, ignoreCase = true) ||
                                    (categoryName == "All" && uiState.selectedCategory.isEmpty())

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (isSelected) {
                                            if (isDark) ElectricPurple else ElectricPurple
                                        } else {
                                            if (isDark) Color(0x401C1538) else Color(0x66DDD6FE)
                                        }
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) NeonCyan else if (isDark) DarkGlassBorderStart else LightGlassBorder,
                                        RoundedCornerShape(20.dp)
                                    )
                                    .clickable { viewModel.updateSelectedCategory(categoryName) }
                                    .padding(horizontal = 16.dp, vertical = 9.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        painter = painterResource(id = iconRes),
                                        contentDescription = null,
                                        tint = if (isSelected) Color.White else if (isDark) DarkTextSecondary else LightTextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = categoryName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else if (isDark) DarkTextSecondary else LightTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                // Section Header: "Recommended for You" + "See all"
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recommended for You",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) DarkTextPrimary else LightTextPrimary
                        )
                        Text(
                            text = "See all",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) CyberBlue else ElectricPurple
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
                            CircularProgressIndicator(color = if (isDark) NeonCyan else ElectricPurple)
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

                // Event Cards List
                items(uiState.recommendedEvents) { event ->
                    NeonEventCard(
                        event = event,
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
                                    color = if (isDark) DarkTextPrimary else LightTextPrimary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Check back later or ask the AI Assistant for recommendations!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isDark) DarkTextSecondary else LightTextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Rich Event Card matching Photo 1 & Photo 4
 */
@Composable
fun NeonEventCard(
    event: Event,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalThemeIsDark.current

    // Generate dynamic banner gradient based on event category / title
    val bannerGradient = when (event.category.lowercase()) {
        "music" -> SunsetCardGradient
        "tech" -> TechCardGradient
        else -> Brush.verticalGradient(listOf(ElectricPurple, CyberBlue))
    }

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
                // Subtle overlay pattern
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x20000000))
                )

                // Overlaid Price Pill on bottom-right (Free or ₹499)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (event.price <= 0) Color(0xCC10B981) else Color(0xCC3A86FF))
                        .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (event.price > 0) "₹${event.price.toInt()}" else "Free",
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                // Category pill on top-left
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0x99090514))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = event.category.ifBlank { "Event" },
                        color = NeonCyan,
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
                    color = if (isDark) DarkTextPrimary else LightTextPrimary
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
                        tint = if (isDark) DarkTextSecondary else LightTextSecondary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = event.date.ifBlank { "Upcoming Date" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDark) DarkTextSecondary else LightTextSecondary
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_mylocation),
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = if (isDark) DarkTextSecondary else LightTextSecondary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = event.location.ifBlank { "Venue" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDark) DarkTextSecondary else LightTextSecondary
                    )
                }

                if (event.availableSeats > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "🔥 ${event.availableSeats} seats left",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isDark) NeonCyan else ElectricPurple,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
