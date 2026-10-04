package com.angelma

import io.ktor.http.ContentType
import io.ktor.server.application.Application
import io.ktor.server.http.content.staticFiles
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    val config = readServerConfig()
    routing {
        get("/health") {
            call.respond(mapOf("status" to "ok"))
        }
        staticFiles("/media", config.mediaDir) {
            enableAutoHeadResponse()
            contentType { file ->
                when(file.extension) {
                    "ts" -> ContentType("video", "mp2t")
                    else -> null
                }
            }
        }
    }
}