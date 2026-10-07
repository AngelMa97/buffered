package com.angelma.feature.player.presentation

data class PlayerState(
    val title: String = "",
    val isLoading: Boolean = true,
    val isPlaying: Boolean = true,
    val codecs: List<String> = emptyList(),
    val selectedCodec: String = "",
    val renditions: List<QualityOption> = emptyList(),
    val selectedRendition: QualityOption? = null
)
