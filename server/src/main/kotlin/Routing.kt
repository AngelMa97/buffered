package com.angelma

import com.angelma.mappers.toVideoSummaryDto
import com.angelma.responses.HealthResponse
import com.angelma.responses.Status
import io.ktor.http.ContentType
import io.ktor.server.application.Application
import io.ktor.server.http.content.staticFiles
import io.ktor.server.plugins.origin
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    val config = readServerConfig()
    val catalogRepository: CatalogRepository = FileCatalogRepository(config.mediaDir)

    routing {
        get("/health") {
            val response = HealthResponse(
                status = Status.OK,
                videos = catalogRepository.getAllVideos().size
            )
            call.respond(response)
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
        get("api/videos") {
            val videos = catalogRepository.getAllVideos()

            val origin = call.request.origin
            val posterRoot = "${origin.scheme}://${origin.serverHost}:${origin.serverPort}/media"

            val summaryVideos = videos.map { video -> video.toVideoSummaryDto("${posterRoot}/${video.id}/${video.poster}") }

            call.respond(summaryVideos)
        }
    }
}