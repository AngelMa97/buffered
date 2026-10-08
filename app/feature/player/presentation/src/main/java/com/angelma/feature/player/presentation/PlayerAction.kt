package com.angelma.feature.player.presentation

sealed interface PlayerAction {
    data object OnBackTap : PlayerAction
    data object OnPause : PlayerAction
    data object OnTogglePlayPauseTap : PlayerAction
    data class OnDragVideoProgress(val progress: Long) : PlayerAction
    data object OnSeekBackTap : PlayerAction
    data object OnSeekForwardTap : PlayerAction
    data class OnSelectedRendition(val selection: QualitySelection) : PlayerAction
    data object OnShowAnalyticsChangeValue : PlayerAction
}