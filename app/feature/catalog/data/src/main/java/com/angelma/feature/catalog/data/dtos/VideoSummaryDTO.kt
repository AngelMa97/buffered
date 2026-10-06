package com.angelma.feature.catalog.data.dtos

import com.angelma.feature.catalog.domain.models.VideoSummary
import kotlinx.serialization.Serializable

@Serializable
data class VideoSummaryDTO(
    val id: String,
    val title: String,
    val year: Int,
    val durationSeconds: Int,
    val posterUrl: String
)

fun VideoSummaryDTO.toVideoSummary() = VideoSummary(
    id = id,
    title = title,
    year = year,
    durationSeconds = durationSeconds,
    posterUrl = posterUrl
)