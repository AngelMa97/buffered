package com.angelma.feature.player.presentation

import androidx.annotation.OptIn
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DecoderReuseEvaluation
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.source.MediaLoadData
import androidx.navigation.toRoute
import com.angelma.feature.player.presentation.util.qualityLabel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class PlayerViewModel(
    savedStateHandle: SavedStateHandle,
    vmExoPlayer: ExoPlayer
) : ViewModel() {

    private val streamUrl = savedStateHandle.toRoute<PlayerGraph>().streamUrl
    private val title = savedStateHandle.toRoute<PlayerGraph>().title

    private var bufferJob: Job? = null

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

            PlayerAction.OnShowAnalyticsChangeValue -> setAnalyticsVisibility()

            else -> Unit
        }
    }

    @OptIn(UnstableApi::class)
    private fun exoPlayerSetup() {
        exoPlayer.apply {
            val mediaItem = MediaItem.Builder().setUri(streamUrl).build()
            setMediaItem(mediaItem)
            prepare()

            playWhenReady = true

            addAnalyticsListener(object : AnalyticsListener {
                override fun onDownstreamFormatChanged(
                    eventTime: AnalyticsListener.EventTime,
                    mediaLoadData: MediaLoadData
                ) {
                    super.onDownstreamFormatChanged(eventTime, mediaLoadData)
                    mediaLoadData.trackFormat?.let { format ->
                        state = state.copy(
                            statsForNerds = state.statsForNerds.copy(
                                downloading = qualityLabel(format.width, format.height)
                            )
                        )
                    }
                }

                override fun onVideoInputFormatChanged(
                    eventTime: AnalyticsListener.EventTime,
                    format: Format,
                    decoderReuseEvaluation: DecoderReuseEvaluation?
                ) {
                    super.onVideoInputFormatChanged(eventTime, format, decoderReuseEvaluation)
                    state = state.copy(
                        statsForNerds = state.statsForNerds.copy(
                            onScreen = qualityLabel(format.width, format.height)
                        )
                    )
                }

                override fun onBandwidthEstimate(
                    eventTime: AnalyticsListener.EventTime,
                    totalLoadTimeMs: Int,
                    totalBytesLoaded: Long,
                    bitrateEstimate: Long
                ) {
                    super.onBandwidthEstimate(
                        eventTime,
                        totalLoadTimeMs,
                        totalBytesLoaded,
                        bitrateEstimate
                    )
                    state = state.copy(
                        statsForNerds = state.statsForNerds.copy(
                            bandwidthBps = bitrateEstimate
                        )
                    )
                }

                override fun onDroppedVideoFrames(
                    eventTime: AnalyticsListener.EventTime,
                    droppedFrames: Int,
                    elapsedMs: Long
                ) {
                    super.onDroppedVideoFrames(eventTime, droppedFrames, elapsedMs)
                    state = state.copy(
                        statsForNerds = state.statsForNerds.copy(
                            droppedFrames = state.statsForNerds.droppedFrames + droppedFrames
                        )
                    )
                }

                override fun onVideoDecoderInitialized(
                    eventTime: AnalyticsListener.EventTime,
                    decoderName: String,
                    initializedTimestampMs: Long,
                    initializationDurationMs: Long
                ) {
                    super.onVideoDecoderInitialized(
                        eventTime,
                        decoderName,
                        initializedTimestampMs,
                        initializationDurationMs
                    )
                    state = state.copy(
                        statsForNerds = state.statsForNerds.copy(
                            codec = decoderName
                        )
                    )
                }
            })

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

    private fun setAnalyticsVisibility() {
        val visible = !state.isStatsForNerdsVisible
        state = state.copy(isStatsForNerdsVisible = visible)

        bufferJob?.cancel()
        if (visible) {
            bufferJob = viewModelScope.launch {
                while (true) {
                    state = state.copy(
                        statsForNerds = state.statsForNerds.copy(
                            bufferMs = exoPlayer.totalBufferedDuration
                        )
                    )
                    delay(500.milliseconds)
                }
            }
        }
    }
}
