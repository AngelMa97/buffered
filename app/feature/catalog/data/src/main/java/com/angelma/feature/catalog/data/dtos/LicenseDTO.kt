package com.angelma.feature.catalog.data.dtos

import com.angelma.feature.catalog.domain.models.License
import kotlinx.serialization.Serializable

@Serializable
data class LicenseDTO(
    val name: String,
    val url: String
)

fun LicenseDTO.toLicense() = License(
    name = name,
    url = url
)
