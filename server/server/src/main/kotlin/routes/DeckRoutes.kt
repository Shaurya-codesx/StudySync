package com.example.routes

import com.example.services.DeckService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID
import com.example.dto.DeckCreateRequest

fun Route.deckRoutes(deckService: DeckService) {
    // Everything inside this block requires a valid JWT token
    authenticate("jwt") {

        route("/decks") {

            // 1. Get all decks for the logged-in user
            get {
                val principal = call.principal<JWTPrincipal>()
                val userIdStr = principal?.payload?.getClaim("userId")?.asString()

                if (userIdStr == null) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                    return@get
                }

                val userId = UUID.fromString(userIdStr)
                val decks = deckService.getAllDecksForUser(userId)

                call.respond(HttpStatusCode.OK, decks)
            }

            // 2. Get a single deck by its ID
            get("{id}") {
                val principal = call.principal<JWTPrincipal>()
                val userIdStr = principal?.payload?.getClaim("userId")?.asString()
                val deckIdStr = call.parameters["id"]

                if (userIdStr == null || deckIdStr == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing data"))
                    return@get
                }

                val userId = UUID.fromString(userIdStr)
                val deckId = try {
                    UUID.fromString(deckIdStr)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid Deck ID format"))
                    return@get
                }

                val deck = deckService.getDeckDetails(deckId, userId)

                if (deck == null) {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Deck not found or access denied"))
                } else {
                    call.respond(HttpStatusCode.OK, deck)
                }
            }

            // 3. Delete a deck
            delete("{id}") {
                val principal = call.principal<JWTPrincipal>()
                val userIdStr = principal?.payload?.getClaim("userId")?.asString()
                val deckIdStr = call.parameters["id"]

                if (userIdStr == null || deckIdStr == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing data"))
                    return@delete
                }

                val userId = UUID.fromString(userIdStr)
                val deckId = try {
                    UUID.fromString(deckIdStr)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid Deck ID format"))
                    return@delete
                }

                val deleted = deckService.deleteDeck(deckId, userId)

                if (deleted) {
                    call.respond(HttpStatusCode.NoContent) // 204 means successful deletion
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Deck not found or access denied"))
                }
            }

            // 4. Create a new deck manually
            post {
                val principal = call.principal<JWTPrincipal>()
                val userIdStr = principal?.payload?.getClaim("userId")?.asString()

                if (userIdStr == null) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                    return@post
                }

                val userId = UUID.fromString(userIdStr)

                val request = try {
                    call.receive<DeckCreateRequest>()
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid JSON body"))
                    return@post
                }

                if (request.title.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Title cannot be empty"))
                    return@post
                }

                val newDeck = deckService.createDeck(userId, request.title)
                call.respond(HttpStatusCode.Created, newDeck)
            }
        }
    }
}