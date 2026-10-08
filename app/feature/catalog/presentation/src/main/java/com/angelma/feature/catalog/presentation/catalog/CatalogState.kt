package com.angelma.feature.catalog.presentation.catalog

import com.angelma.core.presentation.UiText
import com.angelma.feature.catalog.domain.models.VideoSummary

data class CatalogState(
    val isLoading: Boolean = true,
    val error: UiText? = null,
    val videos: List<VideoSummary> = emptyList()
)
