package com.prayogizan.golekpilem.ui.common

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Applies a smooth skeleton shimmer loading animation across the composable.
 *
 * Implements a continuous linear gradient horizontal translation (`0f` to `1000f`)
 * across a 1200ms cycle using [rememberInfiniteTransition]. Automatically adapts shimmer
 * gradient colors based on the active dark or light theme:
 * - Dark Mode: `#2C2C2C` -> `#3A3A3A` -> `#2C2C2C`
 * - Light Mode: `#E0E0E0` -> `#F5F5F5` -> `#E0E0E0`
 *
 * @return A [Modifier] drawing the animated shimmer brush as background.
 */
fun Modifier.shimmerEffect(): Modifier = composed {
    val isDark = isSystemInDarkTheme()
    val shimmerColors = if (isDark) {
        listOf(
            Color(0xFF2C2C2C),
            Color(0xFF3A3A3A),
            Color(0xFF2C2C2C)
        )
    } else {
        listOf(
            Color(0xFFE0E0E0),
            Color(0xFFF5F5F5),
            Color(0xFFE0E0E0)
        )
    }

    val transition = rememberInfiniteTransition(label = "ShimmerTransition")
    val translateAnimation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ShimmerTranslate"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(x = translateAnimation - 300f, y = translateAnimation - 300f),
        end = Offset(x = translateAnimation, y = translateAnimation)
    )

    this.background(brush)
}
