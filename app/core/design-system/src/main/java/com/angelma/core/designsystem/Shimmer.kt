package com.angelma.core.designsystem

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/** Loading placeholder: a light band that sweeps across the surface color. */
fun Modifier.shimmer(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(tween(1_200, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmerProgress"
    )
    val base = BufferedSurfaceHigh
    val highlight = BufferedSurfaceHighest.copy(alpha = 1f)
    background(
        Brush.linearGradient(
            colors = listOf(base, highlight, base),
            start = Offset(progress * 600f, 0f),
            end = Offset(progress * 600f + 600f, 600f),
        )
    )
}

@Preview
@Composable
private fun ShimmerPreview() {
    BufferedTheme {
        Box(
            Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .aspectRatio(2f / 3f)
                .background(BufferedSurfaceHigh, RoundedCornerShape(8.dp))
                .shimmer()
        )
    }
}
