package com.angelma.mappers

import com.angelma.dtos.VideoDto
import com.angelma.dtos.VideoSummaryDto
import com.angelma.models.Video
import com.angelma.records.VideoRecord

fun VideoRecord.toVideo(): Video = Video(
    id = id,
    title = title,
    year = year,
    description = description,
    durationSeconds = durationSeconds,
    master = master,
    poster = poster,
    backdrop = backdrop,
    license = license.toLicense(),
    attribution = attribution,
    renditions = renditions.map { rendition -> rendition.toRendition() }
)

fun Video.toVideoSummaryDto(posterUrl: String) = VideoSummaryDto(
    id = id,
    title = title,
    year = year,
    durationSeconds = durationSeconds,
    posterUrl = posterUrl
)

fun Video.toVideoDto(posterUrl: String, backdropUrl: String, streamUrl: String) = VideoDto(
    id = id,
    title = title,
    year = year,
    description = description,
    durationSeconds = durationSeconds,
    posterUrl = posterUrl,
    backdropUrl = backdropUrl,
    streamUrl = streamUrl,
    license = license.toLicenseDto(),
    attribution = attribution,
    renditions = renditions.map { rendition -> rendition.toRenditionDto() }
)