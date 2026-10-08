package com.angelma.feature.catalog.data.dtos

import com.angelma.feature.catalog.domain.models.VideoDetail
import kotlinx.serialization.Serializable

@Serializable
data class VideoDetailDTO(
    val id: String,
    val title: String,
    val year: Int,
    val description: String,
    val durationSeconds: Int,
    val posterUrl: String,
    val backdropUrl: String,
    val streamUrl: String,
    val license: LicenseDTO,
    val attribution: String,
    val renditions: List<RenditionDTO>
)

fun VideoDetailDTO.toVideoDetail() = VideoDetail(
    id = id,
    title = title,
    year = year,
    description = description,
    durationSeconds = durationSeconds,
    posterUrl = posterUrl,
    backdropUrl = backdropUrl,
    streamUrl = streamUrl,
    license = license.toLicense(),
    attribution = attribution,
    renditions = renditions.map { x -> x.toRendition() }
)
