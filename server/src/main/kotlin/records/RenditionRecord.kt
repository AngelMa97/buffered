package com.angelma.records

import kotlinx.serialization.Serializable

@Serializable
class RenditionRecord(
    val name: String,
    val width: Int,
    val height: Int,
    val peakBandwidth: Int,
    val averageBandwidth: Int,
    val codecs: String,
    val playlist: String
) {
}