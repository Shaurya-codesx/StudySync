package com.example.studysyncandroid.data.remote

import com.example.studysyncandroid.data.remote.dto.AuthResponse
import com.example.studysyncandroid.data.remote.dto.LoginRequest
import com.example.studysyncandroid.data.remote.dto.SignupRequest
import com.example.studysyncandroid.data.remote.dto.SignupResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
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
}