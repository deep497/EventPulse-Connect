package com.example.eventpulse.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Shared modifier for expired / past items:
 * - Desaturates (greyscale / low saturation)
 * - Lowers contrast and opacity to 0.6f
 * - Informs screen readers that this element is disabled
 */
fun Modifier.expiredStyle(isExpired: Boolean): Modifier = if (isExpired) {
    this
        .graphicsLayer {
            alpha = 0.65f
            val matrix = ColorMatrix().apply { setToSaturation(0f) }
            colorFilter = ColorFilter.colorMatrix(matrix)
        }
        .semantics { disabled() }
} else {
    this
}

/**
 * Reusable badge pill for ended, expired, or attended states
 * using theme semantic tokens (no hardcoded colours).
 */
@Composable
fun ExpiredBadge(
    text: String = "Ended",
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Box(
        modifier = modifier
            .background(
                color = containerColor,
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 1.dp,
                color = contentColor.copy(alpha = 0.3f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            color = contentColor
        )
    }
}
