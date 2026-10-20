package com.example.eventpulse.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.eventpulse.ui.theme.*

/**
 * Premium container card with subtle border and warm shadow (Sunset Coral spec).
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    borderBrush: Brush = if (LocalThemeIsDark.current) GlassBorderBrush else Brush.linearGradient(listOf(UserLightBorder, UserLightBorder)),
    borderWidth: Dp = 1.dp,
    containerColor: Color = if (LocalThemeIsDark.current) DarkGlassCard else Color.White,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = if (LocalThemeIsDark.current) 8.dp else 3.dp,
                shape = shape,
                spotColor = if (LocalThemeIsDark.current) Color(0x44DE748F) else Color(0x14000000)
            )
            .border(width = borderWidth, brush = borderBrush, shape = shape),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(content = content)
    }
}

/**
 * Standard app gradient background wrapper (Warm off-white in light mode, deep espresso/plum in dark mode).
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
 * Primary action button with warm Sunset Coral / Plum gradient.
 */
@Composable
fun GradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    gradient: Brush = if (LocalThemeIsDark.current) PlumRoseGradient else SunsetCoralGradient,
    shape: Shape = RoundedCornerShape(16.dp)
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = if (enabled) 6.dp else 0.dp,
                shape = shape,
                spotColor = if (LocalThemeIsDark.current) RosePlum.copy(alpha = 0.4f) else CoralPrimary.copy(alpha = 0.35f)
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
 * Modern animated sliding toggle button for Light and Dark mode.
 */
@Composable
fun DarkLightModeToggle(
    modifier: Modifier = Modifier
) {
    val isDark = LocalThemeIsDark.current
    val thumbOffset by animateDpAsState(
        targetValue = if (isDark) 28.dp else 2.dp,
        animationSpec = tween(durationMillis = 220),
        label = "thumbOffset"
    )

    Box(
        modifier = modifier
            .width(58.dp)
            .height(30.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(if (isDark) Color(0xFF2D222B) else Color(0xFFF2E9EE))
            .border(
                1.dp,
                if (isDark) Color(0xFF453342) else Color(0xFFE2D6DF),
                RoundedCornerShape(15.dp)
            )
            .clickable { ThemeState.toggle() }
            .padding(2.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        // Track icons
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "☀️", fontSize = 11.sp)
            Text(text = "🌙", fontSize = 11.sp)
        }

        // Sliding thumb
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(24.dp)
                .shadow(elevation = 3.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(if (isDark) RosePlum else CoralPrimary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isDark) "🌙" else "☀️",
                fontSize = 10.sp
            )
        }
    }
}
