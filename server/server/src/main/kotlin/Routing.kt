package com.example

import com.example.routes.authRoutes
import com.example.routes.healthRoutes
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun Application.configureRouting() {
    routing {
        // Our active routes
        healthRoutes()
        authRoutes()

        get("/") {
            call.respondText("Hello, World!")
        }

        // Our new bouncer-protected route
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