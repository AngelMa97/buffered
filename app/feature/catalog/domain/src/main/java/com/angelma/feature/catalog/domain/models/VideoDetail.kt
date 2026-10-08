package com.angelma.feature.catalog.domain.models

data class VideoDetail(
    val id: String,
    val title: String,
    val year: Int,
    val description: String,
    val durationSeconds: Int,
    val posterUrl: String,
    val backdropUrl: String,
    val streamUrl: String,
    val license: License,
    val attribution: String,
    val renditions: List<Rendition>
) {
    val isHd get() = renditions.any { it.name == "1080p" }
}
