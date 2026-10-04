package com.angelma.dtos

import kotlinx.serialization.Serializable

@Serializable
data class VideoDto(
    val id: String,
    val title: String,
    val year: Int,
    val description: String,
    val durationSeconds: Int,
    val posterUrl: String,
    val backdropUrl: String,
    val streamUrl: String,
    val license: LicenseDto,
    val attribution: String,
    val renditions: List<RenditionDto>
)
