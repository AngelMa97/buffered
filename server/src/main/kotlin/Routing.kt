package com.angelma

import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    val config = readServerConfig()
    routing {
        get("/health") {
            call.respond(mapOf("status" to "ok"))
        }
    }
}