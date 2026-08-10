package com.example.routes

import com.example.services.DeckService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

fun Route.marketplaceRoutes(deckService: DeckService) {
    route("/marketplace/decks") {

        authenticate("jwt") {
            // GET /marketplace/decks - Paginated public decks
            get {
                val principal = call.principal<JWTPrincipal>()
                val userIdStr = principal?.payload?.getClaim("userId")?.asString()
                
                if (userIdStr == null) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                    return@get
                }

                val userId = UUID.fromString(userIdStr)

                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20

                if (page < 1 || limit < 1 || limit > 100) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid pagination parameters"))
                    return@get
                }

                val paginatedResponse = deckService.getPublicDecks(userId, page, limit)
                call.respond(HttpStatusCode.OK, paginatedResponse)
            }

            // POST /marketplace/decks/{id}/clone - Clone a public deck
            post("{id}/clone") {
                val principal = call.principal<JWTPrincipal>()
                val userIdStr = principal?.payload?.getClaim("userId")?.asString()
                val deckIdStr = call.parameters["id"]

                if (userIdStr == null || deckIdStr == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing data"))
                    return@post
                }

                val userId = UUID.fromString(userIdStr)
                val deckId = try {
                    UUID.fromString(deckIdStr)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid Deck ID format"))
                    return@post
                }

                try {
                    val clonedDeck = deckService.clonePublicDeck(deckId, userId)
                    call.respond(HttpStatusCode.Created, clonedDeck)
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to (e.message ?: "Deck not found or not public")))
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to clone deck"))
                }
            }
        }
    }
}
