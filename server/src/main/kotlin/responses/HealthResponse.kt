package com.angelma.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HealthResponse(
    val status: Status,
    val videos: Int
)

@Serializable
enum class Status {
    @SerialName("ok")
    OK,
    @SerialName("not_ok")
    NOT_OK
}
