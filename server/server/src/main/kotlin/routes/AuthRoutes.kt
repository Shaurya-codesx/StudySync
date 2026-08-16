package com.example.routes

import com.example.models.LoginRequest
import com.example.models.LoginResponse
import com.example.models.RefreshRequest
import com.example.models.RegisterRequest
import com.example.models.SignupResponse
import com.example.models.VerifyEmailRequest
import com.example.models.ResendVerificationRequest
import com.example.models.ForgotPasswordRequest
import com.example.models.ResetPasswordRequest
import com.example.services.AuthService
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.HttpStatusCode

fun Route.authRoutes() {
    val authService = AuthService()

    route("/auth") {
        post("/signup") {
            val request = call.receive<RegisterRequest>()

            try {
                val newUserId = authService.signup(request.email, request.password, request.displayName)

                if (newUserId != null) {
                    call.respond(
                        HttpStatusCode.Created,
                        SignupResponse(
                            userId = newUserId.toString(),
                            email = request.email,
                            displayName = request.displayName
                        )
                    )
                } else {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to "Failed to create user.")
                    )
                }
            } catch (e: IllegalArgumentException) {
                if (e.message == "USER_ALREADY_EXISTS") {
                    call.respond(HttpStatusCode.Conflict, mapOf("error" to "User with this email already exists."))
                } else {
                    call.respondText(e.message ?: "Bad Request", status = HttpStatusCode.BadRequest)
                }
            }
        }

        post("/login") {
            val request = call.receive<LoginRequest>()

            try {
                val tokens = authService.login(request.email, request.password)
                if (tokens != null) {
                    call.respond(HttpStatusCode.OK, LoginResponse(tokens.first, tokens.second))
                } else {
                    call.respondText("Invalid email or password", status = HttpStatusCode.Unauthorized)
                }
            } catch (e: IllegalArgumentException) {
                if (e.message == "EMAIL_NOT_VERIFIED") {
                    call.respondText("Email not verified", status = HttpStatusCode.Forbidden)
                } else {
                    call.respondText(e.message ?: "Bad Request", status = HttpStatusCode.BadRequest)
                }
            }
        }

        post("/refresh") {
            val request = call.receive<RefreshRequest>()
            val tokens = authService.refresh(request.refreshToken)

            if (tokens != null) {
                call.respond(HttpStatusCode.OK, LoginResponse(tokens.first, tokens.second))
            } else {
                call.respondText("Invalid or expired refresh token", status = HttpStatusCode.Unauthorized)
            }
        }

        post("/verify-email") {
            val request = call.receive<VerifyEmailRequest>()
            try {
                val tokens = authService.verifyEmail(request.email, request.otp)
                if (tokens != null) {
                    call.respond(HttpStatusCode.OK, LoginResponse(tokens.first, tokens.second))
                } else {
                    call.respondText("Failed to verify", status = HttpStatusCode.InternalServerError)
                }
            } catch (e: IllegalArgumentException) {
                call.respondText(e.message ?: "Invalid OTP", status = HttpStatusCode.BadRequest)
            }
        }

        post("/resend-verification") {
            val request = call.receive<ResendVerificationRequest>()
            try {
                authService.sendVerificationOtp(request.email)
                call.respond(HttpStatusCode.OK, mapOf("message" to "Verification email sent"))
            } catch (e: Exception) {
                call.respondText("Failed to send email", status = HttpStatusCode.InternalServerError)
            }
        }

        post("/forgot-password") {
            val request = call.receive<ForgotPasswordRequest>()
            try {
                authService.sendResetPasswordOtp(request.email)
                call.respond(HttpStatusCode.OK, mapOf("message" to "Reset email sent"))
            } catch (e: IllegalArgumentException) {
                // Don't leak if user exists or not, just return OK usually, but for study project it's fine
                call.respondText(e.message ?: "Error", status = HttpStatusCode.BadRequest)
            }
        }

        post("/reset-password") {
            val request = call.receive<ResetPasswordRequest>()
            try {
                authService.resetPassword(request.email, request.otp, request.newPassword)
                call.respond(HttpStatusCode.OK, mapOf("message" to "Password reset successful"))
            } catch (e: IllegalArgumentException) {
                call.respondText(e.message ?: "Invalid OTP", status = HttpStatusCode.BadRequest)
            }
        }
    }
}