package com.angelma.mappers

import com.angelma.models.Video
import com.angelma.records.VideoRecord

fun VideoRecord.toVideo(): Video = Video(
    id = id,
    title = title,
    year = year,
    description = description,
    durationSeconds = durationSeconds,
    master = master,
    posterUrl = poster,
    backdropUrl = backdrop,
    license = license.toLicense(),
    attribution = attribution,
    renditions = renditions.map { rendition -> rendition.toRendition() }
)