package com.angelma.feature.player.presentation

data class PlayerState(
    val title: String = "",
    val isPlaying: Boolean = true,
    val renditions: List<QualityOption> = emptyList(),
    val selectedQuality: QualitySelection = QualitySelection.Auto,
    val isStatsForNerdsVisible: Boolean = false,
    val statsForNerds: StatsForNerds = StatsForNerds()
)
