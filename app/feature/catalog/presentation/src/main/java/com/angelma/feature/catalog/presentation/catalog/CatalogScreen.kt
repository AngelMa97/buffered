package com.angelma.feature.catalog.presentation.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.angelma.core.designsystem.BufferedBackground
import com.angelma.core.designsystem.BufferedTheme
import com.angelma.core.presentation.sharedVideoImage
import com.angelma.feature.catalog.domain.models.VideoSummary
import com.angelma.feature.catalog.presentation.R
import org.koin.androidx.compose.koinViewModel

@Composable
fun CatalogScreenRoot(
    onVideoSelected: (videoId: String, posterUrl: String) -> Unit,
    viewModel: CatalogViewModel = koinViewModel()
) {
    CatalogScreen(
        onAction = { action ->
            when (action) {
                is CatalogAction.OnVideoSelect -> onVideoSelected(action.videoId,action.poster)
                else -> viewModel.onAction(action)
            }
        },
        state = viewModel.state
    )
}

@Composable
fun CatalogScreen(
    onAction: (CatalogAction) -> Unit,
    state: CatalogState
) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (state.isLoading) {
            CircularProgressIndicator()
        } else {
            if (state.error != null) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = state.error.asString(),
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = { onAction(CatalogAction.OnRequestVideos) }
                    ) { Text(stringResource(R.string.retry)) }
                }
            } else {
                LazyVerticalGrid(
                    modifier = Modifier.fillMaxSize(),
                    columns = GridCells.Fixed(2)
                ) {
                    stickyHeader {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BufferedBackground)
                                .padding(8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.video_catalog),
                                fontSize = 24.sp
                            )
                        }
                    }
                    items(
                        state.videos,
                        key = { video -> video.id }
                    ) { video ->
                        AsyncImage(
                            modifier = Modifier
                                .fillMaxWidth()
                                .sharedVideoImage(video.id)
                                .padding(4.dp)
                                .aspectRatio(2f / 3f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onAction(CatalogAction.OnVideoSelect(video.id, video.posterUrl))
                                },
                            model = video.posterUrl,
                            contentDescription = video.title,
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CatalogScreenPreview() {
    BufferedTheme {
        CatalogScreen(
            {},
            CatalogState(
                isLoading = false,
//                error = DataError.Network.NO_INTERNET.asUiText(),
                videos = listOf(
                    VideoSummary(
                        id = "TODO()",
                        title = "video 1",
                        year = 2025,
                        durationSeconds = 1,
                        posterUrl = "TODO()"
                    ),
                    VideoSummary(
                        id = "TODO()",
                        title = "video 2",
                        year = 2025,
                        durationSeconds = 1,
                        posterUrl = "TODO()"
                    ),
                    VideoSummary(
                        id = "TODO()",
                        title = "Video 3",
                        year = 2025,
                        durationSeconds = 1,
                        posterUrl = "TODO()"
                    )
                )
            )
        )
    }
}