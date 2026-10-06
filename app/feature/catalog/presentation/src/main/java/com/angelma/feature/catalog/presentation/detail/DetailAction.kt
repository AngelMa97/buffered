package com.angelma.feature.catalog.presentation.detail

sealed interface DetailAction {
    data object OnBackTap : DetailAction
    data object OnPlayVideo : DetailAction
}