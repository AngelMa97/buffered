package com.angelma.feature.player.presentation

import androidx.activity.compose.LocalActivity
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.modifiers.resizeWithContentScale
import androidx.media3.ui.compose.state.rememberPresentationState
import com.angelma.core.designsystem.BufferedTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun PlayerScreenRoot(
    onBack: () -> Unit,
    viewModel: PlayerViewModel = koinViewModel()
) {
    PlayerScreen(
        onAction = { action ->
            when(action) {
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
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        exoPlayer.let { player ->
            val activity = LocalActivity.current
            val presentationState = rememberPresentationState(player)
            PlayerSurface(
                player = player,
                modifier = Modifier
                    .resizeWithContentScale(
                        contentScale = ContentScale.Fit,
                        sourceSizeDp = presentationState.videoSizeDp
                    )
            )

            LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
                if (activity?.isChangingConfigurations == false) {
                    onAction(PlayerAction.OnPause)
                }
            }
        }
    }
}

@Preview
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