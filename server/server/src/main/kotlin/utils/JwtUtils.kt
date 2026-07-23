package com.example.utils

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import java.util.Date

object JwtUtils {
    // Pulls the secret from your .env file, or uses a fallback for local testing
    private val secret = System.getenv("JWT_SECRET") ?: "super-secret-development-key"
    private val algorithm = Algorithm.HMAC256(secret)

    fun generateAccessToken(userId: Int, email: String): String {
        // Defaults to 15 minutes if the environment variable is missing
        val expiryMinutes = System.getenv("JWT_ACCESS_EXPIRY_MINUTES")?.toLongOrNull() ?: 15L
        val expirationTime = Date(System.currentTimeMillis() + expiryMinutes * 60000)

        return JWT.create()
            .withClaim("userId", userId)
            .withClaim("email", email)
            .withExpiresAt(expirationTime)
            .sign(algorithm)
    }

    fun generateRefreshToken(userId: Int): String {
        // Defaults to 30 days if the environment variable is missing
        val expiryDays = System.getenv("JWT_REFRESH_EXPIRY_DAYS")?.toLongOrNull() ?: 30L
        val expirationTime = Date(System.currentTimeMillis() + expiryDays * 24 * 60 * 60 * 1000)

        return JWT.create()
            .withClaim("userId", userId)
            .withExpiresAt(expirationTime)
            .sign(algorithm)
    }

    fun verifyToken(token: String): DecodedJWT? {
        return try {
            // This automatically validates the signature and the expiration date!
            JWT.require(algorithm).build().verify(token)
        } catch (e: Exception) {
            // If the token is fake, altered, or expired, it throws an exception
            null
        }
    }
}