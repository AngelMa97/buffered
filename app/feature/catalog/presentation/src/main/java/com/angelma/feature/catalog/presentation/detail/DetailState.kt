package com.angelma.feature.catalog.presentation.detail

import com.angelma.core.presentation.UiText
import com.angelma.feature.catalog.domain.models.VideoDetail

data class DetailState(
    val videoId: String = "",
    val posterUrl: String = "",
    val videoDetail: VideoDetail? = null,
    val isLoading: Boolean = false,
    val error: UiText? = null
)
