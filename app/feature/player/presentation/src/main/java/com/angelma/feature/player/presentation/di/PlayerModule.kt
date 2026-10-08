package com.angelma.feature.player.presentation.di

import android.annotation.SuppressLint
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.mediacodec.MediaCodecInfo
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector
import com.angelma.feature.player.presentation.PlayerViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

@SuppressLint("UnsafeOptInUsageError")
val playerModule = module {
    factory<ExoPlayer> {
        val renderers = DefaultRenderersFactory(androidContext())
        renderers.setMediaCodecSelector { mimeType, requiresSecureDecoder, requiresTunnelingDecoder ->
            val codecs = MediaCodecSelector.DEFAULT.getDecoderInfos(
                mimeType,
                requiresSecureDecoder,
                requiresTunnelingDecoder
            )

            val (goodCodecs, badCodecs) = codecs.partition { !KNOW_BAD_CODECS.contains(it.name) }

            goodCodecs + badCodecs
        }

        ExoPlayer.Builder(androidContext())
            .setRenderersFactory(renderers)
            .setSeekBackIncrementMs(10_000)
            .setSeekForwardIncrementMs(10_000)
            .build()
    }
    viewModelOf(::PlayerViewModel)
}

private val KNOW_BAD_CODECS = setOf("c2.goldfish.h264.decoder")