package com.angelma.feature.player.presentation

data class PlayerState(
    val isLoading: Boolean = true,
    val codecs: List<String> = emptyList(),
    val selectedCodec: String = "",
    val renditions: List<String> = emptyList(),
    val selectedRendition: String = ""
)
