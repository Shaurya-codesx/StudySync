package com.example

import com.example.routes.healthRoutes
import com.example.services.UserService // <-- New Import
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.resources.*
import io.ktor.server.resources.*
import io.ktor.server.resources.Resources
import kotlinx.serialization.Serializable
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.time.Duration

fun Application.configureRouting() {

    // Initialize our new service
    val userService = UserService()

    routing {
        healthRoutes()

        get("/") {
            call.respondText("Hello, World!")
        }

        // --- NEW ROUTE ---
        post("/register-test") {
            // For now, we are hardcoding the data just to prove the database insert works!
            val newUserId = userService.createUser("testuser", "supersecretpassword")

            if (newUserId != null) {
                call.respondText("Success! User created with ID: $newUserId")
            } else {
                call.respondText("Failed to create user.")
            }
        }
        // -----------------

        get<Articles> { article ->
            call.respond("List of articles sorted starting from ${article.sort}")
        }

        webSocket("/ws") {
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    val text = frame.readText()
                    outgoing.send(Frame.Text("YOU SAID: $text"))
                    if (text.equals("bye", ignoreCase = true)) {
                        close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))
                    }
                }
            }
        }

        get("/json/kotlinx-serialization") {
            call.respond(mapOf("hello" to "world"))
        }
    }
}