package com.angelma.feature.player.presentation

sealed interface QualitySelection {
    data object Auto : QualitySelection
    data object DataSaver : QualitySelection
    data class Fixed(val option: QualityOption) : QualitySelection
}