package com.angelma.records

import kotlinx.serialization.Serializable

@Serializable
class VideoRecord(
    val id: String,
    val title: String,
    val year: Int,
    val description: String,
    val durationSeconds: Int,
    val license: LicenseRecord,
    val attribution: String,
    val sourceUrl: String,
    val master: String,
    val poster: String,
    val backdrop: String,
    val renditions: List<RenditionRecord>,
    val trimmedForTesting: Boolean
) {
}