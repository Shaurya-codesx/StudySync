package com.example.routes

import com.example.dto.ReviewCardRequest
import com.example.dto.ReviewCardResponse
import com.example.repositories.CardRepository
import com.example.services.CardNotFoundException
import com.example.services.CardService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

fun Route.cardRoutes(cardRepository: CardRepository, cardService: CardService) {
    authenticate("jwt") {
        get("/decks/{id}/cards") {
            val deckIdStr = call.parameters["id"]

            if (deckIdStr == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing deck ID"))
                return@get
            }

            val deckId = try {
                UUID.fromString(deckIdStr)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid Deck ID format"))
                return@get
            }

            val cards = cardRepository.getCardsForDeck(deckId)
            call.respond(HttpStatusCode.OK, cards)
        }

        post("/cards/{id}/review") {
            val cardIdStr = call.parameters["id"]

            if (cardIdStr == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing card ID"))
                return@post
            }

            val cardId = try {
                UUID.fromString(cardIdStr)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid Card ID format"))
                return@post
            }

            val principal = call.principal<JWTPrincipal>()
            val userIdStr = principal?.payload?.getClaim("userId")?.asString()

            if (userIdStr == null) {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid or missing token"))
                return@post
            }
            val userId = UUID.fromString(userIdStr)

            val request = try {
                call.receive<ReviewCardRequest>()
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid request body"))
                return@post
            }

            try {
                val result = cardService.reviewCard(cardId, userId, request.quality)
                call.respond(
                    HttpStatusCode.OK,
                    ReviewCardResponse(
                        id = result.id.toString(),
                        easeFactor = result.easeFactor,
                        intervalDays = result.intervalDays,
                        dueDate = result.dueDate.toString()
                    )
                )
            } catch (e: CardNotFoundException) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to (e.message ?: "Card not found")))
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to (e.message ?: "Invalid quality value")))
            }
        }
    }
}