package com.angelma.feature.catalog.domain.models

data class VideoSummary(
    val id: String,
    val title: String,
    val year: Int,
    val durationSeconds: Int,
    val posterUrl: String
)
