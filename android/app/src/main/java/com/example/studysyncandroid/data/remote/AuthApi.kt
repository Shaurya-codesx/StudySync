package com.example.studysyncandroid.data.remote

import com.example.studysyncandroid.data.remote.dto.AuthResponse
import com.example.studysyncandroid.data.remote.dto.LoginRequest
import com.example.studysyncandroid.data.remote.dto.SignupRequest
import com.example.studysyncandroid.data.remote.dto.MessageResponse
import com.example.studysyncandroid.data.remote.dto.VerifyEmailRequest
import com.example.studysyncandroid.data.remote.dto.ResendVerificationRequest
import com.example.studysyncandroid.data.remote.dto.ForgotPasswordRequest
import com.example.studysyncandroid.data.remote.dto.ResetPasswordRequest
import com.example.studysyncandroid.data.remote.dto.UserProfileResponse
import com.example.studysyncandroid.data.remote.dto.SignupResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.get
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import javax.inject.Inject

class AuthApi @Inject constructor(
    private val client: HttpClient
) {
    suspend fun signup(email: String, password: String, displayName: String): SignupResponse =
        client.post("/auth/signup") {
            contentType(ContentType.Application.Json)
            setBody(SignupRequest(email, password, displayName))
        }.body()

    suspend fun login(email: String, password: String): AuthResponse =
        client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(email, password))
        }.body()

    suspend fun verifyEmail(email: String, otp: String): AuthResponse =
        client.post("/auth/verify-email") {
            contentType(ContentType.Application.Json)
            setBody(VerifyEmailRequest(email, otp))
        }.body()

    suspend fun resendVerification(email: String): MessageResponse =
        client.post("/auth/resend-verification") {
            contentType(ContentType.Application.Json)
            setBody(ResendVerificationRequest(email))
        }.body()

    suspend fun forgotPassword(email: String): MessageResponse =
        client.post("/auth/forgot-password") {
            contentType(ContentType.Application.Json)
            setBody(ForgotPasswordRequest(email))
        }.body()

    suspend fun resetPassword(email: String, otp: String, newPassword: String): MessageResponse =
        client.post("/auth/reset-password") {
            contentType(ContentType.Application.Json)
            setBody(ResetPasswordRequest(email, otp, newPassword))
        }.body()

    suspend fun getUserProfile(): UserProfileResponse =
        client.get("/user/profile").body()
}