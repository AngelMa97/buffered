package com.angelma.feature.player.presentation

import androidx.media3.common.TrackGroup

data class QualityOption(
    val trackGroup: TrackGroup,
    val index: Int,
    val quality: String
)
