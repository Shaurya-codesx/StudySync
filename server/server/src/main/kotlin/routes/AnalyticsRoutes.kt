package com.example.routes

import com.example.services.AnalyticsService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

fun Route.analyticsRoutes(analyticsService: AnalyticsService) {
    authenticate("jwt") {
        route("/analytics") {
            // GET /analytics/retention
            get("/retention") {
                val principal = call.principal<JWTPrincipal>()
                val userIdStr = principal?.payload?.getClaim("userId")?.asString()

                if (userIdStr == null) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                    return@get
                }

                val userId = try {
                    UUID.fromString(userIdStr)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid User ID format"))
                    return@get
                }

                val retentionCurve = analyticsService.getRetentionCurve(userId)
                call.respond(HttpStatusCode.OK, retentionCurve)
            }
        }
    }
}
