package com.angelma.records

import kotlinx.serialization.Serializable

@Serializable
class Catalog(
    val generatedAt: String,
    val videos: List<VideoRecord>
)