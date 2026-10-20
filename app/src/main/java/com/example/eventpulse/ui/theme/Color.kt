package com.example.eventpulse.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ==========================================
// 1. SUNSET CORAL THEME PALETTE
// "Warm and festive. Users get coral to amber, organisers get plum."
// ==========================================

// Core User Accents: Coral to Amber
val CoralPrimary = Color(0xFFE03E2D)        // Solid festive Coral Red
val CoralAccent = Color(0xFFFF5722)         // Sunset Coral
val CoralSoft = Color(0xFFFF6B4A)           // Soft warm coral for dark mode
val AmberGold = Color(0xFFFF8A00)           // Sunset Amber
val AmberBright = Color(0xFFFFA726)         // Amber Glow
val SunsetGold = Color(0xFFFFB703)          // Warm Gold

// Core Organiser Accents: Royal Plum to Rose Plum
val RoyalPlum = Color(0xFF7B2382)           // Rich Royal Plum
val RoyalPlumDark = Color(0xFF6B1D71)
val PlumLightTint = Color(0xFFF7EEF8)       // Soft plum background for light buttons
val PlumBorder = Color(0xFFE5D2E7)          // Subtle plum border
val RosePlum = Color(0xFFDE748F)            // Warm Rose-Plum for dark mode
val RosePlumDark = Color(0xFF2E1C2E)

// Backward compat
val ElectricPurple = RoyalPlum
val NeonCyan = CoralAccent
val CyberBlue = Color(0xFF3A86FF)
val SunsetOrange = CoralAccent
val SunsetCoral = CoralPrimary

// Status Badges (Matches reference photo)
val StatusPublished = Color(0xFF10B981)     // Mint Emerald Green
val StatusPublishedBg = Color(0xFFE6F9F0)   // Light Mint Pill Bg
val StatusPending = Color(0xFFF59E0B)       // Warm Amber
val StatusPendingBg = Color(0xFFFEF3E2)     // Light Amber Pill Bg
val StatusRejected = Color(0xFFEF4444)      // Ruby Red
val StatusRejectedBg = Color(0xFFFEE2E2)

// ==========================================
// 2. USER CANVAS & SURFACES (Light & Dark)
// ==========================================
// Light Mode (Warm off-white / ivory canvas)
val UserLightBackgroundStart = Color(0xFFFAF7F5)
val UserLightBackgroundEnd = Color(0xFFF5F0EC)
val UserLightCard = Color(0xFFFFFFFF)
val UserLightBorder = Color(0xFFEDE7E2)
val UserLightTextPrimary = Color(0xFF1F1A17)
val UserLightTextSecondary = Color(0xFF7E7369)
val UserLightTextTertiary = Color(0xFFA3998F)
val UserLightSearchField = Color(0xFFFFFFFF)

// Dark Mode (Deep espresso warm dark canvas)
val UserDarkBackgroundStart = Color(0xFF1A1614)
val UserDarkBackgroundEnd = Color(0xFF14110F)
val UserDarkCard = Color(0xFF26201D)
val UserDarkBorder = Color(0xFF3D3530)
val UserDarkTextPrimary = Color(0xFFF7F2ED)
val UserDarkTextSecondary = Color(0xFFA1958C)
val UserDarkTextTertiary = Color(0xFF6B6159)
val UserDarkSearchField = Color(0xFF25201D)

// ==========================================
// 3. ORGANISER CANVAS & SURFACES (Light & Dark)
// ==========================================
// Light Mode (Warm soft plum/lavender canvas)
val OrgLightBackgroundStart = Color(0xFFFAF6F9)
val OrgLightBackgroundEnd = Color(0xFFF5EEF4)
val OrgLightCard = Color(0xFFFFFFFF)
val OrgLightBorder = Color(0xFFEDE2EF)
val OrgLightTextPrimary = Color(0xFF231726)
val OrgLightTextSecondary = Color(0xFF7A6B7E)
val OrgLightTextTertiary = Color(0xFFA091A4)

// Dark Mode (Deep warm dark plum-charcoal canvas)
val OrgDarkBackgroundStart = Color(0xFF171219)
val OrgDarkBackgroundEnd = Color(0xFF120E14)
val OrgDarkCard = Color(0xFF251B27)
val OrgDarkBorder = Color(0xFF3F2D44)
val OrgDarkTextPrimary = Color(0xFFF9F3FA)
val OrgDarkTextSecondary = Color(0xFFA897AB)
val OrgDarkTextTertiary = Color(0xFF726275)

// General Glass / Light / Dark compatibility aliases
val DarkBackgroundStart = Color(0xFF181416)
val DarkBackgroundEnd = Color(0xFF120F11)
val DarkGlassCard = Color(0xFF241C23)
val DarkGlassCardHover = Color(0xFF2E222D)
val DarkGlassBorderStart = Color(0xFF423042)
val DarkGlassBorderEnd = Color(0xFFDE748F)
val DarkTextPrimary = Color(0xFFF9F4F7)
val DarkTextSecondary = Color(0xFFA89CA7)
val DarkTextTertiary = Color(0xFF6E646E)
val DarkSearchField = Color(0xFF251D24)

val LightBackgroundStart = Color(0xFFFAF7F6)
val LightBackgroundEnd = Color(0xFFF4EFF1)
val LightGlassCard = Color(0xFFFFFFFF)
val LightGlassBorder = Color(0xFFEDE5EB)
val LightTextPrimary = Color(0xFF1F1821)
val LightTextSecondary = Color(0xFF786A7B)
val LightTextTertiary = Color(0xFFA194A3)
val LightSearchField = Color(0xFFFFFFFF)

// ==========================================
// 4. GRADIENTS
// ==========================================
val DarkBackgroundGradient = Brush.verticalGradient(
    colors = listOf(DarkBackgroundStart, DarkBackgroundEnd)
)

val LightBackgroundGradient = Brush.verticalGradient(
    colors = listOf(LightBackgroundStart, LightBackgroundEnd)
)

// Sunset Coral Banner Gradient (Matches Reference Photo 2 Hero card)
val SunsetHeroGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFFFF5722), Color(0xFFFF8A00))
)

val SunsetDarkHeroGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFFFF6B4A), Color(0xFFFFA726))
)

// Buttons & Actions
val SunsetCoralGradient = Brush.horizontalGradient(
    colors = listOf(CoralPrimary, AmberGold)
)

val PlumButtonGradient = Brush.horizontalGradient(
    colors = listOf(RoyalPlum, Color(0xFF9C27B0))
)

val PlumRoseGradient = Brush.horizontalGradient(
    colors = listOf(RosePlum, RoyalPlum)
)

val NeonButtonGradient = SunsetCoralGradient
val PurpleCyanGradient = Brush.horizontalGradient(
    colors = listOf(RoyalPlum, CoralPrimary)
)

val GlassBorderBrush = Brush.linearGradient(
    colors = listOf(DarkGlassBorderStart, DarkGlassBorderEnd.copy(alpha = 0.5f))
)

val AiButtonGradient = Brush.linearGradient(
    colors = listOf(RoyalPlum, Color(0xFF9C27B0))
)

val SunsetCardGradient = SunsetHeroGradient
val TechCardGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF8A9A36), Color(0xFFA5B838))
)