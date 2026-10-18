package com.example.eventpulse.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.eventpulse.ui.theme.*

/**
 * Premium glassmorphic container card with glowing border and subtle gradient background.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    borderBrush: Brush = if (LocalThemeIsDark.current) GlassBorderBrush else Brush.linearGradient(listOf(LightGlassBorder, LightGlassBorder)),
    borderWidth: Dp = 1.dp,
    containerColor: Color = if (LocalThemeIsDark.current) DarkGlassCard else LightGlassCard,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = if (LocalThemeIsDark.current) 12.dp else 4.dp,
                shape = shape,
                spotColor = if (LocalThemeIsDark.current) Color(0x667B2CBF) else Color(0x1A000000)
            )
            .border(width = borderWidth, brush = borderBrush, shape = shape),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(content = content)
    }
}

/**
 * Standard app gradient background wrapper (Deep Space Violet/Navy in dark mode, soft lavender in light mode).
 */
@Composable
fun AppBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = LocalThemeIsDark.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush = if (isDark) DarkBackgroundGradient else LightBackgroundGradient),
        content = content
    )
}

/**
 * Glowing primary action button with horizontal gradient.
 */
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    gradient: Brush = PurpleCyanGradient,
    shape: Shape = RoundedCornerShape(16.dp)
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = if (enabled) 10.dp else 0.dp,
                shape = shape,
                spotColor = if (LocalThemeIsDark.current) NeonCyan.copy(alpha = 0.5f) else ElectricPurple.copy(alpha = 0.4f)
            )
            .clip(shape)
            .background(if (enabled) gradient else Brush.linearGradient(listOf(Color.Gray, Color.DarkGray)))
            .clickable(enabled = enabled && !isLoading, onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = Color.White,
                strokeWidth = 2.5.dp
            )
        } else {
            Text(
                text = text,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

/**
 * Compact Dark / Light mode toggle icon button suitable for TopAppBars.
 */
@Composable
fun DarkLightModeToggle(
    modifier: Modifier = Modifier
) {
    val isDark = LocalThemeIsDark.current
    IconButton(
        onClick = { ThemeState.toggle() },
        modifier = modifier
            .clip(CircleShape)
            .background(if (isDark) DarkGlassCard else LightGlassCard)
            .border(1.dp, if (isDark) DarkGlassBorderStart else LightGlassBorder, CircleShape)
    ) {
        Icon(
            painter = painterResource(
                id = if (isDark) android.R.drawable.btn_star_big_on else android.R.drawable.btn_star_big_off
            ),
            contentDescription = "Toggle Dark/Light Mode",
            tint = if (isDark) SunsetGold else ElectricPurple
        )
    }
}
