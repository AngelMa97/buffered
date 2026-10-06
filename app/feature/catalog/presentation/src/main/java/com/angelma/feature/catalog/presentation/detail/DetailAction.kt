package com.angelma.feature.catalog.presentation.detail

sealed interface DetailAction {
    data object OnBackTap : DetailAction
    data class OnPlayVideo(val streamUrl: String, val title: String) : DetailAction
    data object OnRequestVideoDetail: DetailAction
}