package com.example.services

import com.example.models.Users
import com.example.repositories.UserRepository
import com.example.utils.JwtUtils
import com.example.utils.PasswordUtils

class AuthService {
    // Bring in the repository so we can talk to the database
    private val userRepository = UserRepository()

    // 1. Signup Logic
    fun signup(emailInput: String, passwordInput: String, displayNameInput: String): Int? {
        // Hash the password BEFORE it goes to the repository
        val hashedPassword = PasswordUtils.hashPassword(passwordInput)
        return userRepository.insertUser(emailInput, hashedPassword, displayNameInput)
    }

    // 2. Login Logic
    fun login(emailInput: String, passwordInput: String): Pair<String, String>? {
        // Find the user by email
        val userRow = userRepository.findByEmail(emailInput) ?: return null

        // Extract the saved data from the Postgres row
        val savedHash = userRow[Users.passwordHash]
        val userId = userRow[Users.id]

        // Check if the provided password matches the database hash
        if (PasswordUtils.verifyPassword(passwordInput, savedHash)) {
            // Passwords match! Generate the digital passports (JWTs)
            val accessToken = JwtUtils.generateAccessToken(userId, emailInput)
            val refreshToken = JwtUtils.generateRefreshToken(userId)

            // Return both tokens together
            return Pair(accessToken, refreshToken)
        }

        // Passwords did not match
        return null
    }

    // 3. Refresh Logic
    fun refresh(refreshTokenInput: String): Pair<String, String>? {
        // 1. Verify the refresh token is valid and not expired
        val decoded = JwtUtils.verifyToken(refreshTokenInput) ?: return null

        // 2. Extract the user ID from the token payload
        val userId = decoded.getClaim("userId").asInt() ?: return null

        // 3. Find the user in the database to get their email
        val userRow = userRepository.findById(userId) ?: return null
        val email = userRow[Users.email]

        // 4. Generate fresh tokens
        val newAccessToken = JwtUtils.generateAccessToken(userId, email)
        val newRefreshToken = JwtUtils.generateRefreshToken(userId)

        return Pair(newAccessToken, newRefreshToken)
    }
}