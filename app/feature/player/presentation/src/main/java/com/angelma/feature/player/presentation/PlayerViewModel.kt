package com.angelma.feature.player.presentation

import androidx.annotation.OptIn
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.toRoute

class PlayerViewModel(
    savedStateHandle: SavedStateHandle,
    vmExoPlayer: ExoPlayer
) : ViewModel() {

    private val streamUrl = savedStateHandle.toRoute<PlayerGraph>().streamUrl
    private val title = savedStateHandle.toRoute<PlayerGraph>().title

    private val BOXES = listOf(
        "360p" to (640 to 360),
        "480p" to (854 to 480),
        "540p" to (960 to 540),
        "720p" to (1280 to 720),
        "1080p" to (1920 to 1080),
    )

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
            is PlayerAction.OnSelectedRendition -> {
                if (action.selection == state.selectedQuality) return
                state = state.copy(selectedQuality = action.selection)
                when (val selection = action.selection) {
                    QualitySelection.Auto -> setupQuality(null)
                    QualitySelection.DataSaver -> setMaxQuality()
                    is QualitySelection.Fixed -> setupQuality(selection.option)
                }
            }

            else -> Unit
        }
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

                override fun onTracksChanged(tracks: Tracks) {
                    super.onTracksChanged(tracks)
                    for (group in tracks.groups) {
                        if (group.type == C.TRACK_TYPE_VIDEO) {
                            for (i in 0 until group.length) {
                                val format = group.getTrackFormat(i)
                                val quality = qualityLabel(format.width, format.height)
                                val qualityItem = QualityOption(group.mediaTrackGroup, i, quality)
                                if (!state.renditions.contains(qualityItem)) {
                                    state = state.copy(
                                        renditions = state.renditions + qualityItem
                                    )
                                }
                            }
                        }
                    }
                }
            })

        }
    }

    private fun moveVideoTo(progress: Long) {
        exoPlayer.seekTo(progress)
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

    private fun setupQuality(quality: QualityOption?) {
        val builder = exoPlayer.trackSelectionParameters
            .buildUpon()
            .clearOverridesOfType(C.TRACK_TYPE_VIDEO)
            .clearVideoSizeConstraints()

        if (quality != null) {
            val mOverride = TrackSelectionOverride(quality.trackGroup, quality.index)
            builder.addOverride(mOverride)
        }

        exoPlayer.trackSelectionParameters = builder.build()

    }

    @OptIn(UnstableApi::class)
    private fun setMaxQuality() {
        val parameters = exoPlayer.trackSelectionParameters
            .buildUpon()
            .clearOverridesOfType(C.TRACK_TYPE_VIDEO)
            .setMaxVideoSize(1280, 720)
            .build()

        exoPlayer.trackSelectionParameters = parameters
    }

    private fun qualityLabel(width: Int, height: Int): String =
        BOXES.firstOrNull { (_, box) -> width <= box.first && height <= box.second }?.first
            ?: "${height}p"
}
