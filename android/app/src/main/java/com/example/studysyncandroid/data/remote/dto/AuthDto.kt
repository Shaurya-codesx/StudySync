package com.example.studysyncandroid.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SignupRequest(
    val email: String,
    val password: String,
    val displayName: String
)

@Serializable
data class SignupResponse(
    val userId: String,
    val email: String,
    val displayName: String
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String
)

@Serializable
data class RefreshRequest(
    val refreshToken: String
)