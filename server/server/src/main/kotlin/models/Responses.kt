package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val accessToken: String,
    val refreshToken: String
)

@Serializable
data class ErrorResponse(
    val error: String
)