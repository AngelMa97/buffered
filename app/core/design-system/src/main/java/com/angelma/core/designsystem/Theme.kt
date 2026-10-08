package com.angelma.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = BufferedOrange,
    onPrimary = BufferedBackground,
    primaryContainer = BufferedOrangeContainer,
    onPrimaryContainer = BufferedOnOrangeContainer,
    secondary = BufferedOrange,
    onSecondary = BufferedBackground,
    secondaryContainer = BufferedOrangeContainer,
    onSecondaryContainer = BufferedOnOrangeContainer,
    tertiary = BufferedOrange,
    onTertiary = BufferedBackground,
    background = BufferedBackground,
    onBackground = BufferedText,
    surface = BufferedBackground,
    onSurface = BufferedText,
    surfaceVariant = BufferedSurfaceVariant,
    onSurfaceVariant = BufferedTextMuted,
    surfaceContainerLowest = BufferedBackground,
    surfaceContainerLow = BufferedSurfaceLow,
    surfaceContainer = BufferedSurface,
    surfaceContainerHigh = BufferedSurfaceHigh,
    surfaceContainerHighest = BufferedSurfaceHighest,
    surfaceBright = BufferedSurfaceHighest,
    surfaceDim = BufferedBackground,
    outline = BufferedOutline,
    outlineVariant = BufferedSurfaceHigh,
    error = BufferedError,
    onError = BufferedBackground,
)

@Composable
fun BufferedTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

@Preview
@Composable
private fun BufferedThemePreview() {
    BufferedTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Sintel", style = MaterialTheme.typography.titleLarge)
                Text(
                    text = "2010 · CC BY 3.0 · Blender Foundation",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Card {
                    Text("Card", modifier = Modifier.padding(12.dp))
                }
                Button(onClick = {}) { Text("Play") }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(Quality1080, Quality720, Quality540, Quality360).forEach { QualitySwatch(it) }
                }
            }
        }
    }
}

@Composable
private fun QualitySwatch(color: Color) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .background(color, RoundedCornerShape(4.dp))
    )
}
