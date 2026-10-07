package com.angelma.feature.catalog.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.angelma.core.designsystem.BufferedBackground
import com.angelma.core.designsystem.BufferedTheme
import com.angelma.feature.catalog.domain.models.License
import com.angelma.feature.catalog.domain.models.Rendition
import com.angelma.feature.catalog.domain.models.VideoDetail
import com.angelma.feature.catalog.presentation.R
import org.koin.androidx.compose.koinViewModel
import kotlin.time.Duration.Companion.seconds

@Composable
fun DetailScreenRoot(
    onBack: () -> Unit,
    onVideoPlay: (streamUrl: String, title: String) -> Unit,
    viewModel: DetailViewModel = koinViewModel()
) {
    DetailScreen(
        onAction = { action ->
            when (action) {
                DetailAction.OnBackTap -> onBack()
                is DetailAction.OnPlayVideo -> onVideoPlay(action.streamUrl, action.title)
                else -> viewModel.onAction(action)
            }
        },
        viewModel.state
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    onAction: (DetailAction) -> Unit,
    state: DetailState
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
                        onClick = { onAction(DetailAction.OnRequestVideoDetail) }
                    ) { Text(stringResource(R.string.retry)) }
                }
            } else {
                state.videoDetail?.let { detail ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        AsyncImage(
                            modifier = Modifier
                                .fillMaxWidth(),
                            model = detail.backdropUrl,
                            contentDescription = detail.title
                        )
                        if (detail.isHd) {
                            SuggestionChip(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp),
                                onClick = {},
                                label = {
                                    Text(
                                        text = stringResource(R.string.hd),
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                )
                            )
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(BufferedBackground.copy(alpha = 0.5f))
                                .padding(horizontal = 8.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TopAppBar(
                                title = {},
                                modifier = Modifier
                                    .fillMaxWidth(),
                                navigationIcon = {
                                    IconButton(
                                        onClick = { onAction(DetailAction.OnBackTap) }
                                    ) {
                                        Icon(
                                            modifier = Modifier.size(25.dp),
                                            painter = painterResource(com.angelma.core.presentation.R.drawable.ic_back_arrow),
                                            contentDescription = stringResource(R.string.back)
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = Color.Transparent,
                                    titleContentColor = MaterialTheme.colorScheme.primary
                                ),
                                windowInsets = WindowInsets(0, 0, 0, 0),
                            )
                            IconButton(
                                modifier = Modifier
                                    .padding(vertical = 16.dp)
                                    .size(150.dp)
                                    .align(Alignment.CenterHorizontally),
                                onClick = {
                                    onAction(
                                        DetailAction.OnPlayVideo(
                                            streamUrl = detail.streamUrl,
                                            title = detail.title
                                        )
                                    )
                                },
                            ) {
                                Icon(
                                    modifier = Modifier.fillMaxSize(),
                                    painter = painterResource(id = R.drawable.ic_play_button),
                                    contentDescription = stringResource(R.string.play_video)
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = detail.title,
                                    fontSize = 24.sp
                                )
                            }
                            Text(
                                text = detail.description
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Text(
                                    text = stringResource(R.string.year, detail.year)
                                )
                                Text(
                                    text = stringResource(
                                        R.string.duration,
                                        detail.durationSeconds.seconds
                                    )
                                )
                            }
                            Text(
                                text = detail.license.name,
                                fontSize = 12.sp
                            )
                            Text(
                                text = detail.attribution,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DetailScreenPreview() {
    BufferedTheme {
        DetailScreen(
            {},
            DetailState(
                videoDetail = VideoDetail(
                    id = "",
                    title = "Video title",
                    year = 2026,
                    description = "This is a video very good",
                    durationSeconds = 6000,
                    posterUrl = "",
                    backdropUrl = "",
                    streamUrl = "",
                    license = License(
                        name = "License name",
                        url = "URL"
                    ),
                    attribution = "(c) copyright 2008, Blender Foundation / www.bigbuckbunny.org",
                    renditions = listOf(
                        Rendition(
                            name = "1080p",
                            width = 1024,
                            height = 768,
                            bandwidth = 100000
                        )
                    )
                )
            )
        )
    }
}