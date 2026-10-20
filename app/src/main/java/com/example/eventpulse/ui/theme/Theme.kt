package com.example.eventpulse.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color

// Global state to toggle theme anywhere easily (Defaults to Light mode as requested)
object ThemeState {
    var isDarkMode = mutableStateOf(false)

    fun toggle() {
        isDarkMode.value = !isDarkMode.value
    }

    fun setDarkMode(isDark: Boolean) {
        isDarkMode.value = isDark
    }
}

val LocalThemeIsDark = compositionLocalOf { false }

private val DarkColorScheme = darkColorScheme(
    primary = CoralSoft,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF382320),
    onPrimaryContainer = Color(0xFFFFDBCF),
    secondary = RosePlum,
    onSecondary = Color(0xFF1F121C),
    secondaryContainer = Color(0xFF3B2538),
    onSecondaryContainer = Color(0xFFF6D6F2),
    tertiary = AmberBright,
    onTertiary = Color.Black,
    background = DarkBackgroundStart,
    onBackground = DarkTextPrimary,
    surface = DarkGlassCard,
    onSurface = DarkTextPrimary,
    surfaceVariant = Color(0xFF2C222B),
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkGlassBorderStart,
    outlineVariant = Color(0x33423042),
    error = StatusRejected,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = CoralPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFECE5),
    onPrimaryContainer = Color(0xFF4A1006),
    secondary = RoyalPlum,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF7EEF8),
    onSecondaryContainer = Color(0xFF320736),
    tertiary = AmberGold,
    onTertiary = Color.White,
    background = LightBackgroundStart,
    onBackground = LightTextPrimary,
    surface = LightGlassCard,
    onSurface = LightTextPrimary,
    surfaceVariant = Color(0xFFF5EEF1),
    onSurfaceVariant = LightTextSecondary,
    outline = LightGlassBorder,
    outlineVariant = Color(0x33EDE5EB),
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