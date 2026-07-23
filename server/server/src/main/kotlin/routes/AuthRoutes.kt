package com.example.routes

import com.example.models.LoginRequest
import com.example.models.LoginResponse
import com.example.models.RefreshRequest
import com.example.models.RegisterRequest
import com.example.services.AuthService
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.HttpStatusCode

// We use an extension function on Route to keep things clean
fun Route.authRoutes() {
    val authService = AuthService()

    route("/auth") {
        post("/signup") {
            val request = call.receive<RegisterRequest>()

            // Pass data to the new AuthService
            val newUserId = authService.signup(request.email, request.password, request.displayName)

            if (newUserId != null) {
                call.respondText("Success! User ${request.displayName}, EmailId : ${request.email} created with ID: $newUserId", status = HttpStatusCode.Created)
            } else {
                call.respondText("Failed to create user.", status = HttpStatusCode.InternalServerError)
            }
        }

        post("/login") {
            val request = call.receive<LoginRequest>()

            // Pass credentials to the service for verification
            val tokens = authService.login(request.email, request.password)

            if (tokens != null) {
                // Return the tokens as a clean JSON object
                call.respond(HttpStatusCode.OK, LoginResponse(tokens.first, tokens.second))
            } else {
                // If the email doesn't exist or the password is wrong, return a 401
                call.respondText("Invalid email or password", status = HttpStatusCode.Unauthorized)
            }
        }

        post("/refresh") {
            val request = call.receive<RefreshRequest>()

            // Pass the token to the service
            val tokens = authService.refresh(request.refreshToken)

            if (tokens != null) {
                // Reuse the LoginResponse since the output shape is identical
                call.respond(HttpStatusCode.OK, LoginResponse(tokens.first, tokens.second))
            } else {
                call.respondText("Invalid or expired refresh token", status = HttpStatusCode.Unauthorized)
            }
        }
    }
}