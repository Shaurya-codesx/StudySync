package com.example

import com.example.repositories.CardRepository
import com.example.routes.authRoutes
import com.example.routes.cardRoutes
import com.example.routes.deckRoutes
import com.example.routes.healthRoutes
import com.example.services.AiService
import com.example.services.DeckService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    // 1. Instantiate the dependencies for our new routes
    val deckService = DeckService()
    val aiService = AiService()
    val cardRepository = CardRepository()

    routing {
        // 2. Existing active routes
        healthRoutes()
        authRoutes()

        // 3. Register the new Phase 5 CRUD routes
        deckRoutes(deckService, aiService)
        cardRoutes(cardRepository)

        get("/") {
            call.respondText("Hello, World!")
        }

        // 4. Your existing test route (great for quick debugging!)
        authenticate("jwt") {
            get("/protected-test") {
                val principal = call.principal<JWTPrincipal>()
                val email = principal?.payload?.getClaim("email")?.asString()
                call.respondText("Success! You have accessed a protected route. Your email is: $email")
            }
        }

        // (You can safely leave your WebSocket /ws block here if you still have it!)
    }
}