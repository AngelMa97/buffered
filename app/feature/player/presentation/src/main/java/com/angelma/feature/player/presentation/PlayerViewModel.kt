package com.angelma.feature.player.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.toRoute

class PlayerViewModel(
    savedStateHandle: SavedStateHandle,
    private val vmExoPlayer: ExoPlayer
) : ViewModel() {

    private val streamUrl = savedStateHandle.toRoute<PlayerGraph>().streamUrl
    private val title = savedStateHandle.toRoute<PlayerGraph>().title

    var state by mutableStateOf(PlayerState())
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
        when(action) {
            PlayerAction.OnPause -> pausePlayer()
            else -> Unit
        }
    }

    private fun exoPlayerSetup() {
        exoPlayer.apply {
            val mediaItem = MediaItem.Builder().setUri(streamUrl).build()
            setMediaItem(mediaItem)
            prepare()

            playWhenReady = true
        }
    }

    private fun pausePlayer() {
        exoPlayer.pause()
    }

    private fun togglePlayPause() {
        exoPlayer.let { player ->
            if (player.isPlaying) {
                player.pause()
            } else {
                player.play()
            }
        }
    }
}