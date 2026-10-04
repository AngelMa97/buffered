package com.angelma.dtos

import kotlinx.serialization.Serializable

@Serializable
data class RenditionDto(
    val name: String,
    val width: Int,
    val height: Int,
    val bandwidth: Int
)