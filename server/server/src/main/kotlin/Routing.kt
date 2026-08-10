package com.example

import com.example.repositories.CardRepository
import com.example.repositories.RoomRepository
import com.example.repositories.UserRepository
import com.example.repositories.FolderRepository
import com.example.routes.authRoutes
import com.example.routes.cardRoutes
import com.example.routes.deckRoutes
import com.example.routes.folderRoutes
import com.example.routes.marketplaceRoutes
import com.example.routes.healthRoutes
import com.example.routes.roomRoutes
import com.example.routes.analyticsRoutes
import com.example.services.AiService
import com.example.services.CardService
import com.example.services.DeckService
import com.example.services.RoomService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    // 1. Instantiate the dependencies for our routes
    val deckService = DeckService()
    val aiService = AiService()
    val cardRepository = CardRepository()
    val cardService = CardService(cardRepository)
    val roomRepository = RoomRepository()
    val roomService = RoomService()
    val userRepository = UserRepository()
    val folderRepository = FolderRepository()
    val analyticsRepository = com.example.repositories.AnalyticsRepository()
    val analyticsService = com.example.services.AnalyticsService(analyticsRepository)

    routing {
        // 2. Existing active routes
        healthRoutes()
        authRoutes()

        // 3. CRUD + Phase 7/8 routes
        deckRoutes(deckService, aiService)
        cardRoutes(cardRepository, cardService)
        roomRoutes(roomRepository, roomService, userRepository)
        folderRoutes(folderRepository)
        marketplaceRoutes(deckService)
        analyticsRoutes(analyticsService)

        get("/") {
            call.respondText("Hello, World!")
        }

        // 4. Existing test route
        authenticate("jwt") {
            get("/protected-test") {
                val principal = call.principal<JWTPrincipal>()
                val email = principal?.payload?.getClaim("email")?.asString()
                call.respondText("Success! You have accessed a protected route. Your email is: $email")
            }
        }
    }
}