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
import com.example.dto.UpdateDeckRequest
import java.util.concurrent.ConcurrentHashMap
import com.example.dto.GenerateDeckRequest
import com.example.dto.GeneratedCardResponse
import com.example.dto.GeneratedDeckResponse
import com.example.models.Cards
import com.example.models.Decks
import com.example.services.AiService
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction


// Simple in-memory rate limiter (tracks generations per userId)
val dailyGenerationLimits = ConcurrentHashMap<String, Int>()
val MAX_GENERATIONS_PER_DAY = 5

fun Route.deckRoutes(deckService: DeckService, aiService: AiService) {
    // Everything inside this block requires a valid JWT token
    authenticate("jwt") {

        route("/decks") {

            post("/generate") {
                val principal = call.principal<JWTPrincipal>()
                val userIdStr = principal?.payload?.getClaim("userId")?.asString()

                if (userIdStr == null) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                    return@post
                }

                // 1. Rate Limiting Check
                val currentCount = dailyGenerationLimits.getOrDefault(userIdStr, 0)
                if (currentCount >= MAX_GENERATIONS_PER_DAY) {
                    call.respond(HttpStatusCode.TooManyRequests, mapOf("error" to "Daily generation limit reached (5 decks max)."))
                    return@post
                }

                // 2. Parse and Validate Request
                val request = try {
                    call.receive<GenerateDeckRequest>()
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid JSON body"))
                    return@post
                }

                val sourceText = request.sourceText
                if (sourceText.length < 50) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Notes are too short. Minimum 50 characters."))
                    return@post
                }
                if (sourceText.length > 20000) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Notes are too long. Please split them into smaller chunks."))
                    return@post
                }

                // 3. Call the AI Service
                val aiGeneratedCards = aiService.generateFlashcards(sourceText)

                // 4. Generate a default title from the notes
                val generatedTitle = "AI Deck - " + sourceText.take(25).replace("\n", " ").trim() + "..."

                // 5. Save everything in a single SQL Transaction
                val userUuid = UUID.fromString(userIdStr)

                val generatedDeckResponse = transaction {
                    // Insert the Deck using the correct Kotlin property names from your schema
                    val newDeckId = Decks.insert {
                        it[userId] = userUuid
                        it[title] = generatedTitle
                        it[Decks.sourceText] = sourceText
                    }[Decks.id] // Grab the auto-generated UUID

                    // Batch Insert all the Cards at once
                    val insertedCards = Cards.batchInsert(aiGeneratedCards) { cardDto ->
                        this[Cards.deckId] = newDeckId
                        this[Cards.question] = cardDto.question
                        this[Cards.answer] = cardDto.answer
                        // easeFactor, intervalDays, repetitions, dueDate, and createdAt will use their defaults!
                    }

                    // Map the inserted rows directly to the response DTO
                    val finalCardsList = insertedCards.map { row ->
                        GeneratedCardResponse(
                            id = row[Cards.id].toString(),
                            question = row[Cards.question],
                            answer = row[Cards.answer]
                        )
                    }

                    GeneratedDeckResponse(
                        deckId = newDeckId.toString(),
                        title = generatedTitle,
                        cards = finalCardsList
                    )
                }

                // Increment rate limiter on success
                dailyGenerationLimits[userIdStr] = currentCount + 1

                // 6. Return the success response
                call.respond(HttpStatusCode.Created, generatedDeckResponse)
            }

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

            // 5. Update a deck's folder
            patch("{id}/folder") {
                val principal = call.principal<JWTPrincipal>()
                val userIdStr = principal?.payload?.getClaim("userId")?.asString()
                val deckIdStr = call.parameters["id"]

                if (userIdStr == null || deckIdStr == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing data"))
                    return@patch
                }

                val userId = UUID.fromString(userIdStr)
                val deckId = try {
                    UUID.fromString(deckIdStr)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid Deck ID format"))
                    return@patch
                }

                val request = try {
                    call.receive<UpdateDeckRequest>()
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid JSON body"))
                    return@patch
                }

                val folderId = request.folderId?.let { UUID.fromString(it) }

                val updated = deckService.updateDeckFolder(deckId, userId, folderId)
                if (updated) {
                    call.respond(HttpStatusCode.OK, mapOf("success" to true))
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Deck not found or access denied"))
                }
            }
        }
    }
}