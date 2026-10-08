package com.angelma.feature.catalog.presentation.catalog

sealed interface CatalogAction {
    data class OnVideoSelect(val videoId: String, val poster: String) : CatalogAction
    data object OnRequestVideos : CatalogAction
}