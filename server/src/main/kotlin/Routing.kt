package com.angelma

import com.angelma.Constants.MEDIA_ROUTE
import com.angelma.Constants.VIDEO_NOT_FOUND
import com.angelma.ext.absoluteRoute
import com.angelma.mappers.toVideoDto
import com.angelma.mappers.toVideoSummaryDto
import com.angelma.responses.ErrorResponse
import com.angelma.responses.HealthResponse
import com.angelma.responses.Status
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.http.content.staticFiles
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.util.getOrFail

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
        staticFiles(MEDIA_ROUTE, config.mediaDir) {
            enableAutoHeadResponse()
            contentType { file ->
                when (file.extension) {
                    "ts" -> ContentType("video", "mp2t")
                    else -> null
                }
            }
        }
        get("api/videos") {
            val videos = catalogRepository.getAllVideos()

            val posterRoot =
                "${call.request.absoluteRoute()}$MEDIA_ROUTE"

            val summaryVideos =
                videos.map { video -> video.toVideoSummaryDto("${posterRoot}/${video.id}/${video.poster}") }

            call.respond(summaryVideos)
        }
        get("/api/videos/{id}") {
            val paramId = call.parameters.getOrFail("id")

            val video = catalogRepository.getVideoById(paramId)

            video?.let { video ->

                val mediaRoot =
                    "${call.request.absoluteRoute()}$MEDIA_ROUTE"

                call.respond(
                    video.toVideoDto(
                        posterUrl = "${mediaRoot}/${video.id}/${video.poster}",
                        backdropUrl = "${mediaRoot}/${video.id}/${video.backdrop}",
                        streamUrl = "${mediaRoot}/${video.id}/${video.master}"
                    )
                )

            } ?: call.respond(
                HttpStatusCode.NotFound,
                ErrorResponse(
                    error = VIDEO_NOT_FOUND,
                    message = "No video with id '$paramId'"
                )
            )

        }
    }
}