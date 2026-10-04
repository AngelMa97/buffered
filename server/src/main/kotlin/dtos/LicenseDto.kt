package com.angelma.dtos

import kotlinx.serialization.Serializable

@Serializable
data class LicenseDto(
    val name: String,
    val url: String
)
