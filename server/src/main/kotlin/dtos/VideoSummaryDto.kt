package com.angelma.dtos

import kotlinx.serialization.Serializable

@Serializable
data class VideoSummaryDto(
    val id: String,
    val title: String,
    val year: Int,
    val durationSeconds: Int,
    val posterUrl: String
)
