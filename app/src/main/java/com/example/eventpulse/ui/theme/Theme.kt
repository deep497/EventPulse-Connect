package com.example.eventpulse.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color

// Global state to toggle theme anywhere easily
object ThemeState {
    var isDarkMode = mutableStateOf(true) // Defaults to modern dark neon

    fun toggle() {
        isDarkMode.value = !isDarkMode.value
    }
}

val LocalThemeIsDark = compositionLocalOf { true }

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color(0xFF090514),
    primaryContainer = ElectricPurple,
    onPrimaryContainer = Color.White,
    secondary = CyberBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF261C4C),
    onSecondaryContainer = Color(0xFFD6CEF5),
    tertiary = SunsetOrange,
    onTertiary = Color.White,
    background = DarkBackgroundStart,
    onBackground = DarkTextPrimary,
    surface = DarkGlassCard,
    onSurface = DarkTextPrimary,
    surfaceVariant = Color(0xFF261C4C),
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkGlassBorderStart,
    outlineVariant = Color(0x333E2F75),
    error = StatusRejected,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = ElectricPurple,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDDD6FE),
    onPrimaryContainer = Color(0xFF1E1338),
    secondary = CyberBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0E7FF),
    onSecondaryContainer = Color(0xFF1E1B4B),
    tertiary = SunsetOrange,
    onTertiary = Color.White,
    background = LightBackgroundStart,
    onBackground = LightTextPrimary,
    surface = LightGlassCard,
    onSurface = LightTextPrimary,
    surfaceVariant = Color(0xFFEDE9FE),
    onSurfaceVariant = LightTextSecondary,
    outline = LightGlassBorder,
    outlineVariant = Color(0x33DDD6FE),
    error = StatusRejected,
    onError = Color.White
)

@Composable
fun EventPulseTheme(
    darkTheme: Boolean = ThemeState.isDarkMode.value,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    CompositionLocalProvider(LocalThemeIsDark provides darkTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}