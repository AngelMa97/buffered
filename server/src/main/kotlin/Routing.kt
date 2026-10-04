package com.angelma

import com.angelma.Constants.INVALID_MBPS
import com.angelma.Constants.MEDIA_ROUTE
import com.angelma.Constants.VIDEO_NOT_FOUND
import com.angelma.ext.Throttle
import com.angelma.ext.absoluteRoute
import com.angelma.mappers.toVideoDto
import com.angelma.mappers.toVideoSummaryDto
import com.angelma.responses.ErrorResponse
import com.angelma.responses.HealthResponse
import com.angelma.responses.Status
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.defaultForFile
import io.ktor.server.application.Application
import io.ktor.server.http.content.staticFiles
import io.ktor.server.response.respond
import io.ktor.server.response.respondOutputStream
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.routing
import io.ktor.server.util.getOrFail
import java.io.File

fun Application.configureRouting(
    repository: CatalogRepository,
    mediaDir: File,
    isDemo: Boolean,
    throttle: Throttle
) {
    routing {
        get("/health") {
            val response = HealthResponse(
                status = Status.OK,
                videos = repository.getAllVideos().size
            )
            call.respond(response)
        }
        if (isDemo) {
            put("/demo/throttle") {
                val callMbps = call.request.queryParameters["mbps"]?.toDoubleOrNull()
                if (callMbps != null && !callMbps.isNaN() && callMbps >= 0.0) {
                    throttle.mbps = callMbps
                    call.respond(mapOf("mbps" to callMbps))
                } else {
                    call.respond(
                        HttpStatusCode.BadRequest, ErrorResponse(
                            error = INVALID_MBPS,
                            message = "Please enter a number equal or bigger than 0.0"
                        )
                    )
                }
            }
            get("${MEDIA_ROUTE}/{path...}") {
                val segments = call.parameters.getAll("path").orEmpty()
                val file = resolveMediaFile(mediaDir, segments) ?: return@get call.respond(
                    HttpStatusCode.NotFound
                )

                val type = when(file.extension) {
                    "ts" -> ContentType("video", "mp2t")
                    else -> ContentType.defaultForFile(file)
                }

                call.respondOutputStream(type) {
                    file.inputStream().use { input ->
                        val buffer = ByteArray(16 * 1024)
                        while (true) {
                            val read = input.read(buffer)
                            if (read == -1) break
                            throttle.waitFor(read)
                            write(buffer, 0, read)
                        }
                    }
                }
            }
        } else {
            staticFiles(MEDIA_ROUTE, mediaDir) {
                enableAutoHeadResponse()
                contentType { file ->
                    when (file.extension) {
                        "ts" -> ContentType("video", "mp2t")
                        else -> null
                    }
                }
            }
        }
        get("api/videos") {
            val videos = repository.getAllVideos()

            val posterRoot =
                "${call.request.absoluteRoute()}$MEDIA_ROUTE"

            val summaryVideos =
                videos.map { video -> video.toVideoSummaryDto("${posterRoot}/${video.id}/${video.poster}") }

            call.respond(summaryVideos)
        }
        get("/api/videos/{id}") {
            val paramId = call.parameters.getOrFail("id")

            val video = repository.getVideoById(paramId)

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

fun Application.configureApi() {
    val config = readServerConfig()
    val repository: CatalogRepository = FileCatalogRepository(config.mediaDir)
    val throttle = Throttle()
    configureRouting(repository, config.mediaDir, config.demoEnabled, throttle)
}

fun resolveMediaFile(mediaDir: File, segments: List<String>): File? {
    val root = mediaDir.canonicalFile
    val requested = File(root, segments.joinToString("/")).canonicalFile
    return if (requested.startsWith("${root}/") && requested.isFile) requested
    else null
}