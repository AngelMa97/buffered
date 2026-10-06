package com.angelma.feature.player.presentation

sealed interface PlayerAction {
    data object OnBackTap : PlayerAction
}