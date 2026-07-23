package com.example

import com.example.models.ErrorResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.response.respond
import org.jetbrains.exposed.exceptions.ExposedSQLException

fun Application.configureStatusPages() {
    install(StatusPages) {

        // Catch 409 Conflict: Duplicate Emails from Postgres
        exception<ExposedSQLException> { call, cause ->
            // Check if the crash was caused by our unique index constraint
            if (cause.message?.contains("duplicate key value violates unique constraint") == true ||
                cause.message?.contains("unique_index") == true) {
                call.respond(HttpStatusCode.Conflict, ErrorResponse("This email is already registered."))
            } else {
                // If it's a different database error, fall back to 500
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse("A database error occurred."))
            }
        }

        // Catch 400 Bad Request: Malformed JSON or missing fields
        exception<BadRequestException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, ErrorResponse("Invalid request format or missing fields."))
        }

        // Catch 500 Internal Server Error: The ultimate fallback for everything else
        exception<Throwable> { call, cause ->
            cause.printStackTrace() // Logs it to your Docker terminal for debugging
            call.respond(HttpStatusCode.InternalServerError, ErrorResponse("An unexpected server error occurred."))
        }
    }
}
