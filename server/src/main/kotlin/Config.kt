package com.angelma

import io.ktor.server.application.Application
import java.io.File

data class ServerConfig(val mediaDir: File)

fun Application.readServerConfig(): ServerConfig {
    val mediaRoute = environment.config.property("buffered.mediaDir").getString()

    val mediaFiles = File(mediaRoute)

    check (File(mediaFiles, "catalog.json").isFile) {
        "No catalog found in absolute path ${mediaFiles.absolutePath}, please run pipeline first."
    }

    return ServerConfig(mediaFiles)

}
