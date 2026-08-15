package com.example.routes

import com.example.models.UserProfileResponse
import com.example.repositories.UserRepository
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.HttpStatusCode
import java.util.UUID

fun Route.userRoutes(userRepository: UserRepository) {
    authenticate("jwt") {
        route("/user") {
            get("/profile") {
                val principal = call.principal<JWTPrincipal>()
                val userIdString = principal?.payload?.getClaim("userId")?.asString()
                
                if (userIdString == null) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                    return@get
                }
                
                val userId = try {
                    UUID.fromString(userIdString)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid user ID format"))
                    return@get
                }

                val userRow = userRepository.findById(userId)
                if (userRow != null) {
                    val email = userRow[com.example.models.Users.email]
                    val displayName = userRow[com.example.models.Users.displayName]
                    call.respond(HttpStatusCode.OK, UserProfileResponse(email, displayName))
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "User not found"))
                }
            }
        }
    }
}
