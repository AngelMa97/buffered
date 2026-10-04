package com.angelma

import com.angelma.Constants.INTERNAL_ERROR
import com.angelma.responses.ErrorResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.application.log.error("ERROR", cause)
            call.respond(HttpStatusCode.InternalServerError, ErrorResponse(
                error = INTERNAL_ERROR,
                message = "Something went wrong"
            ))
        }
    }
}
