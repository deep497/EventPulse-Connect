package com.example.eventpulse.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ==========================================
// 1. NEON ACCENT PALETTE (Phase 5 Spec)
// ==========================================
val ElectricPurple = Color(0xFF7B2CBF)
val NeonCyan = Color(0xFF00F5D4)
val CyberBlue = Color(0xFF3A86FF)
val SunsetOrange = Color(0xFFFF5722)
val SunsetCoral = Color(0xFFE64A19)
val SunsetGold = Color(0xFFFFB703)

// Status Pills
val StatusPublished = Color(0xFF10B981)   // Emerald Green
val StatusPending = Color(0xFFF59E0B)     // Amber Gold
val StatusRejected = Color(0xFFEF4444)    // Ruby Red

// ==========================================
// 2. DARK NEON GLASSMORPHIC PALETTE
// ==========================================
// Deep space violet / navy
val DarkBackgroundStart = Color(0xFF090514)
val DarkBackgroundEnd = Color(0xFF150D2A)

// Glass surfaces
val DarkGlassCard = Color(0xD91C1538)     // ~85% opacity #1C1538
val DarkGlassCardHover = Color(0xE6261C4C)
val DarkGlassBorderStart = Color(0xFF3E2F75)
val DarkGlassBorderEnd = Color(0xFF00F5D4)

// Neutral text / borders
val DarkTextPrimary = Color(0xFFFFFFFF)
val DarkTextSecondary = Color(0xFFB3A8D6)
val DarkTextTertiary = Color(0xFF7B719E)
val DarkSearchField = Color(0x661C1538)

// ==========================================
// 3. LIGHT GLASSMORPHIC PALETTE (Complementary)
// ==========================================
val LightBackgroundStart = Color(0xFFF5F3FF) // Soft Lavender-White
val LightBackgroundEnd = Color(0xFFEDE9FE)
val LightGlassCard = Color(0xF2FFFFFF)       // Crisp Translucent White
val LightGlassBorder = Color(0xFFDDD6FE)
val LightTextPrimary = Color(0xFF1E1338)
val LightTextSecondary = Color(0xFF6B5C8E)
val LightTextTertiary = Color(0xFF9E92BC)
val LightSearchField = Color(0xE6FFFFFF)

// ==========================================
// 4. GRADIENT DEFINITIONS
// ==========================================
val DarkBackgroundGradient = Brush.verticalGradient(
    colors = listOf(DarkBackgroundStart, DarkBackgroundEnd)
)

val LightBackgroundGradient = Brush.verticalGradient(
    colors = listOf(LightBackgroundStart, LightBackgroundEnd)
)

val NeonButtonGradient = Brush.horizontalGradient(
    colors = listOf(ElectricPurple, CyberBlue, NeonCyan)
)

val PurpleCyanGradient = Brush.horizontalGradient(
    colors = listOf(ElectricPurple, NeonCyan)
)

val GlassBorderBrush = Brush.linearGradient(
    colors = listOf(DarkGlassBorderStart, DarkGlassBorderEnd.copy(alpha = 0.5f))
)

val AiButtonGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF8A2BE2), Color(0xFF00F5D4), Color(0xFF3A86FF))
)

val SunsetCardGradient = Brush.verticalGradient(
    colors = listOf(SunsetCoral, SunsetOrange, SunsetGold)
)

val TechCardGradient = Brush.verticalGradient(
    colors = listOf(CyberBlue, NeonCyan)
)