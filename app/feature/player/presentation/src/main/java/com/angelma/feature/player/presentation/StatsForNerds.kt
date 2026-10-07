package com.angelma.feature.player.presentation

data class StatsForNerds(
    val downloading: String = "",
    val onScreen: String = "",
    val bandwidthBps: Long = 0L,
    val bufferMs: Long = 0L,
    val droppedFrames: Int = 0,
    val codec: String = ""
)
