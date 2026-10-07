package com.angelma.feature.player.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.toRoute

class PlayerViewModel(
    savedStateHandle: SavedStateHandle,
    vmExoPlayer: ExoPlayer
) : ViewModel() {

    private val streamUrl = savedStateHandle.toRoute<PlayerGraph>().streamUrl
    private val title = savedStateHandle.toRoute<PlayerGraph>().title

    var state by mutableStateOf(PlayerState(title = title))
        private set

    var exoPlayer = vmExoPlayer
        private set

    init {
        exoPlayer = vmExoPlayer
        exoPlayerSetup()
    }

    override fun onCleared() {
        super.onCleared()
        exoPlayer.release()
    }

    fun onAction(action: PlayerAction) {
        when (action) {
            PlayerAction.OnPause -> pausePlayer()
            PlayerAction.OnTogglePlayPauseTap -> togglePlayPause()
            is PlayerAction.OnDragVideoProgress -> moveVideoTo(action.progress)
            PlayerAction.OnSeekBackTap -> exoPlayer.seekBack()
            PlayerAction.OnSeekForwardTap -> exoPlayer.seekForward()
            else -> Unit
        }
    }

    private fun moveVideoTo(progress: Long) {
        exoPlayer.seekTo(progress)
    }

    private fun exoPlayerSetup() {
        exoPlayer.apply {
            val mediaItem = MediaItem.Builder().setUri(streamUrl).build()
            setMediaItem(mediaItem)
            prepare()

            playWhenReady = true

            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    super.onIsPlayingChanged(isPlaying)
                    state = state.copy(isPlaying = isPlaying)
                }
            })
        }
    }

    private fun pausePlayer() {
        exoPlayer.pause()
    }

    private fun togglePlayPause() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            if (exoPlayer.playbackState == Player.STATE_ENDED) exoPlayer.seekTo(0)
            exoPlayer.play()
        }
    }
}