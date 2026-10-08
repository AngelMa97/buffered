package com.angelma.feature.player.presentation.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.angelma.core.designsystem.BufferedTheme

/**
 * Playback bar: position, buffered range and seeking in a single component.
 *
 * The three layers (background, buffer and position) are drawn on the same Canvas inside the
 * Slider's `track`, so they share height, corners and width, and stay aligned with the thumb.
 *
 * @param position playback progress, 0f..1f.
 * @param buffered buffered progress, 0f..1f.
 * @param onSeek called **on release** with the chosen progress (0f..1f). While dragging, the bar
 * follows the finger and nothing is asked of the player.
 * @param onSeekingChange `true` as soon as the finger touches the bar and `false` when it lifts.
 * Lets the controls stay visible while the user is seeking, even if the finger doesn't move.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeekBar(
    position: Float,
    buffered: Float,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onSeekingChange: (Boolean) -> Unit = {},
) {
    var dragValue by remember { mutableStateOf<Float?>(null) }
    val interactionSource = remember { MutableInteractionSource() }
    val isDragged by interactionSource.collectIsDraggedAsState()
    val isPressed by interactionSource.collectIsPressedAsState()

    val isSeeking = isDragged || isPressed
    val currentOnSeekingChange by rememberUpdatedState(onSeekingChange)
    LaunchedEffect(isSeeking) { currentOnSeekingChange(isSeeking) }

    val playedColor = MaterialTheme.colorScheme.primary
    val bufferedColor = Color.White.copy(alpha = 0.45f)
    val trackColor = Color.White.copy(alpha = 0.2f)

    Slider(
        value = dragValue ?: position.coerceIn(0f, 1f),
        onValueChange = { dragValue = it },
        onValueChangeFinished = {
            dragValue?.let(onSeek)
            dragValue = null
        },
        modifier = modifier,
        enabled = enabled,
        interactionSource = interactionSource,
        thumb = {
            val dotSize by animateDpAsState(if (isSeeking) 18.dp else 12.dp)
            Box(modifier = Modifier.size(18.dp), contentAlignment = Alignment.Center) {
                Box(Modifier.size(dotSize).background(playedColor, CircleShape))
            }
        },
        track = { sliderState ->
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
            ) {
                val radius = CornerRadius(size.height / 2)
                drawRoundRect(color = trackColor, size = size, cornerRadius = radius)
                drawRoundRect(
                    color = bufferedColor,
                    size = Size(size.width * buffered.coerceIn(0f, 1f), size.height),
                    cornerRadius = radius
                )
                drawRoundRect(
                    color = playedColor,
                    size = Size(size.width * sliderState.value, size.height),
                    cornerRadius = radius
                )
            }
        }
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0E14)
@Composable
private fun SeekBarPreview() {
    BufferedTheme {
        SeekBar(
            position = 0.25f,
            buffered = 0.53f,
            onSeek = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
