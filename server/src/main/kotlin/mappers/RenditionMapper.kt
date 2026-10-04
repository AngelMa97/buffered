package com.angelma.mappers

import com.angelma.models.Rendition
import com.angelma.records.RenditionRecord

fun RenditionRecord.toRendition() = Rendition(
    name = name,
    width = width,
    height = height,
    bandwidth = peakBandwidth
)