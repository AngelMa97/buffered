package com.angelma.feature.catalog.data.dtos

import com.angelma.feature.catalog.domain.models.Rendition
import kotlinx.serialization.Serializable

@Serializable
data class RenditionDTO(
    val name: String,
    val width: Int,
    val height: Int,
    val bandwidth: Int
)

fun RenditionDTO.toRendition() = Rendition(
    name = name,
    width = width,
    height = height,
    bandwidth = bandwidth
)
