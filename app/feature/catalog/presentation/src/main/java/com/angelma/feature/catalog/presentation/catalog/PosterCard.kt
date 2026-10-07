package com.angelma.feature.catalog.presentation.catalog

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.angelma.core.designsystem.shimmer
import com.angelma.core.presentation.sharedVideoImage
import com.angelma.feature.catalog.domain.models.VideoSummary

/** Catalog poster: fades in when loaded and shrinks slightly while pressed. */
@Composable
fun PosterCard(
    video: VideoSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.95f else 1f, label = "posterScale")

    AsyncImage(
        modifier = modifier
            .fillMaxWidth()
            .sharedVideoImage(video.id)
            .padding(4.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .aspectRatio(2f / 3f)
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = onClick
            ),
        // Crossfade only here: the detail header must not fade when it swaps poster → backdrop,
        // right as the shared transition ends.
        model = ImageRequest.Builder(LocalPlatformContext.current)
            .data(video.posterUrl)
            .crossfade(true)
            .build(),
        contentDescription = video.title,
        contentScale = ContentScale.Crop
    )
}

/** Same footprint as a [PosterCard], shown while the catalog loads. */
@Composable
fun PosterPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp)
            .aspectRatio(2f / 3f)
            .clip(RoundedCornerShape(8.dp))
            .shimmer()
    )
}
