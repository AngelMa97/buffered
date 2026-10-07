@file:kotlin.OptIn(ExperimentalMaterial3Api::class)

package com.angelma.feature.player.presentation

import androidx.activity.compose.LocalActivity
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.indicators.ProgressIndicator
import androidx.media3.ui.compose.modifiers.resizeWithContentScale
import androidx.media3.ui.compose.state.rememberPresentationState
import com.angelma.core.designsystem.BufferedTheme
import com.angelma.feature.player.presentation.components.SeekBar
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import kotlin.time.Duration.Companion.seconds

@Composable
fun PlayerScreenRoot(
    onBack: () -> Unit,
    viewModel: PlayerViewModel = koinViewModel()
) {
    PlayerScreen(
        onAction = { action ->
            when (action) {
                PlayerAction.OnBackTap -> onBack()
                else -> viewModel.onAction(action)
            }
        },
        state = viewModel.state,
        exoPlayer = viewModel.exoPlayer
    )
}

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    onAction: (PlayerAction) -> Unit,
    state: PlayerState,
    exoPlayer: ExoPlayer?
) {
    Box(
        contentAlignment = Alignment.Center
    ) {
        exoPlayer?.let { player ->
            val activity = LocalActivity.current
            val presentationState = rememberPresentationState(player)

            LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
                if (activity?.isChangingConfigurations == false) {
                    onAction(PlayerAction.OnPause)
                }
            }

            PlayerSurface(
                player = player,
                modifier = Modifier
                    .resizeWithContentScale(
                        contentScale = ContentScale.Fit,
                        sourceSizeDp = presentationState.videoSizeDp
                    )
            )

            PlayerUiControl(
                modifier = Modifier.fillMaxSize(),
                onAction = onAction,
                state = state,
                player = player
            )
        }

    }
}

@OptIn(UnstableApi::class)
@Composable
fun PlayerUiControl(
    modifier: Modifier = Modifier,
    onAction: (PlayerAction) -> Unit,
    state: PlayerState,
    player: ExoPlayer
) {
    var controlsVisible by rememberSaveable { mutableStateOf(false) }
    var isSeeking by remember { mutableStateOf(false) }
    val settingsSheet = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    var showSettingsSheet by remember { mutableStateOf(false) }

    LaunchedEffect(controlsVisible, state.isPlaying, isSeeking) {
        if (controlsVisible && state.isPlaying && !isSeeking) {
            delay(2.seconds)
            controlsVisible = false
        }
    }

    Box(
        modifier
            .pointerInput(Unit) {
                detectTapGestures { _ ->
                    controlsVisible = !controlsVisible
                }
            },
    ) {
        AnimatedVisibility(
            modifier = Modifier.fillMaxSize(),
            visible = controlsVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.6f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopStart)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { onAction(PlayerAction.OnBackTap) }
                    ) {
                        Icon(
                            modifier = Modifier.size(30.dp),
                            painter = painterResource(R.drawable.ic_back_arrow),
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                    Text(
                        modifier = Modifier.weight(1f),
                        text = state.title,
                        fontSize = 24.sp
                    )
                    IconButton(
                        modifier = Modifier.size(30.dp),
                        onClick = { showSettingsSheet = true }
                    ) {
                        Icon(
                            modifier = Modifier.size(30.dp),
                            painter = painterResource(R.drawable.ic_settings),
                            contentDescription = stringResource(R.string.settings),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        modifier = Modifier.size(40.dp),
                        onClick = { onAction(PlayerAction.OnSeekBackTap) }
                    ) {
                        Icon(
                            modifier = Modifier.size(40.dp),
                            painter = painterResource(R.drawable.ic_seek_back),
                            contentDescription = stringResource(R.string.return_10s)
                        )
                    }
                    IconButton(
                        modifier = Modifier.size(80.dp),
                        onClick = { onAction(PlayerAction.OnTogglePlayPauseTap) }
                    ) {
                        Icon(
                            modifier = Modifier.size(80.dp),
                            painter = painterResource(if (state.isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
                            contentDescription = if (state.isPlaying) stringResource(R.string.pause) else stringResource(
                                R.string.play
                            )
                        )
                    }
                    IconButton(
                        modifier = Modifier.size(40.dp),
                        onClick = { onAction(PlayerAction.OnSeekForwardTap) }
                    ) {
                        Icon(
                            modifier = Modifier.size(40.dp),
                            painter = painterResource(R.drawable.ic_seek_forward),
                            contentDescription = stringResource(R.string.next_10s)
                        )
                    }
                }

                val totalTicks = rememberSaveable { mutableIntStateOf(0) }

                ProgressIndicator(
                    player = player,
                    totalTickCount = totalTicks.intValue,
                ) {
                    SeekBar(
                        position = currentPositionProgress,
                        buffered = bufferedPositionProgress,
                        onSeek = { progress ->
                            onAction(PlayerAction.OnDragVideoProgress(progressToPosition(progress)))
                        },
                        onSeekingChange = { isSeeking = it },
                        enabled = changingProgressEnabled,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomStart)
                            .padding(vertical = 32.dp, horizontal = 16.dp)
                            .onSizeChanged { totalTicks.intValue = it.width },
                    )
                }
            }
        }
    }

    if (showSettingsSheet) {
        ModalBottomSheet(
            modifier = Modifier
                .widthIn(max = 480.dp),
            onDismissRequest = {
                showSettingsSheet = false
            },
            sheetState = settingsSheet
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SettingsItem(
                    title = stringResource(
                        R.string.quality,
                        qualityLabel(state.selectedQuality)
                    ),
                    options = listOf(
                        QualitySelection.Auto,
                        QualitySelection.DataSaver
                    ) + state.renditions.map { QualitySelection.Fixed(it) },
                    selected = state.selectedQuality,
                    optionLabel = { qualityLabel(it) },
                    onSelect = { selection ->
                        onAction(PlayerAction.OnSelectedRendition(selection))
                    }
                )
                HorizontalDivider()
            }
        }
    }

}

@Composable
fun <T> SettingsItem(
    modifier: Modifier = Modifier,
    title: String,
    options: List<T>,
    selected: T,
    optionLabel: @Composable (T) -> String,
    onSelect: (selectedOption: T) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    Column(modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    isExpanded = !isExpanded
                }
                .padding(vertical = 8.dp)
        ) {
            Text(title)
        }
        if (isExpanded) {
            for (option in options) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSelect(option)
                        }
                        .padding(vertical = 4.dp, horizontal = 8.dp),
                    text = optionLabel(option),
                    color = if (option == selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun qualityLabel(selection: QualitySelection): String = when (selection) {
    QualitySelection.Auto -> stringResource(R.string.auto)
    QualitySelection.DataSaver -> stringResource(R.string.data_saving)
    is QualitySelection.Fixed -> selection.option.quality
}

@Preview(showBackground = true)
@Composable
private fun PlayerScreenPreview() {
    BufferedTheme {
        PlayerScreen(
            {},
            PlayerState(),
            null
        )
    }
}