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

        // Catch HTTP Request Exceptions from Ktor Client (e.g. Gemini 429 Too Many Requests)
        exception<io.ktor.client.plugins.ClientRequestException> { call, cause ->
            if (cause.response.status == HttpStatusCode.TooManyRequests) {
                call.respond(HttpStatusCode.TooManyRequests, ErrorResponse("AI generation rate limit exceeded. Please wait a minute and try again."))
            } else {
                call.respond(cause.response.status, ErrorResponse("AI service error: ${cause.response.status.description}"))
            }
        }

        // Catch 500 Internal Server Error: The ultimate fallback for everything else
        exception<Throwable> { call, cause ->
            cause.printStackTrace() // Logs it to your Docker terminal for debugging
            if (cause.message?.contains("Failed to generate valid flashcards") == true) {
                call.respond(HttpStatusCode.BadGateway, mapOf("error" to "AI generation failed, please try again. The notes might be too complex or malformed."))
            } else {
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse("An unexpected server error occurred."))
            }
        }
    }
}
