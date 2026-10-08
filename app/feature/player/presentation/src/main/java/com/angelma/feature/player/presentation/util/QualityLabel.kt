package com.angelma.feature.player.presentation.util

private val BOXES = listOf(
    "360p" to (640 to 360),
    "480p" to (854 to 480),
    "540p" to (960 to 540),
    "720p" to (1280 to 720),
    "1080p" to (1920 to 1080),
)

internal fun qualityLabel(width: Int, height: Int): String =
    BOXES.firstOrNull { (_, box) -> width <= box.first && height <= box.second }?.first
        ?: "${height}p"
