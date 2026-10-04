package com.angelma.ext

import io.ktor.server.plugins.origin
import io.ktor.server.request.ApplicationRequest

fun ApplicationRequest.absoluteRoute(): String =
    "${origin.scheme}://${origin.serverHost}:${origin.serverPort}"