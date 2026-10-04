package com.angelma.records

import kotlinx.serialization.Serializable

@Serializable
class LicenseRecord(
    val name: String,
    val url: String
) {
}