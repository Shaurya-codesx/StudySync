package com.example.routes

import com.example.repositories.CardRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

fun Route.cardRoutes(cardRepository: CardRepository) {
    authenticate("jwt") {
        // Fetch all cards belonging to a specific deck
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
    }
}