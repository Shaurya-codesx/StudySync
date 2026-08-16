package com.example.services

import com.example.models.Users
import com.example.models.Otps
import com.example.repositories.UserRepository
import com.example.repositories.OtpRepository
import com.example.utils.JwtUtils
import com.example.utils.PasswordUtils
import java.time.LocalDateTime
import java.util.UUID

class AuthService {
    private val userRepository = UserRepository()
    private val otpRepository = OtpRepository()
    private val emailService = EmailService()

    private fun generateOtp(): String {
        return (100000..999999).random().toString()
    }

    suspend fun signup(emailInput: String, passwordInput: String, displayNameInput: String): UUID? {
        val existingUser = userRepository.findByEmail(emailInput)
        val hashedPassword = PasswordUtils.hashPassword(passwordInput)

        if (existingUser != null) {
            val isVerified = existingUser[Users.isVerified]
            if (isVerified) {
                throw IllegalArgumentException("USER_ALREADY_EXISTS")
            } else {
                val userId = existingUser[Users.id]
                userRepository.updatePassword(emailInput, hashedPassword)
                userRepository.updateDisplayName(userId, displayNameInput)
                sendVerificationOtp(emailInput)
                return userId
            }
        } else {
            val userId = userRepository.insertUser(emailInput, hashedPassword, displayNameInput)
            if (userId != null) {
                sendVerificationOtp(emailInput)
            }
            return userId
        }
    }

    suspend fun sendVerificationOtp(email: String) {
        val otp = generateOtp()
        val hashedOtp = PasswordUtils.hashPassword(otp)
        val expiresAt = LocalDateTime.now().plusMinutes(15)
        otpRepository.insertOtp(email, hashedOtp, expiresAt, "verification")
        
        emailService.sendEmail(
            toEmail = email,
            subject = "Verify your StudySync Account",
            body = "Your verification code is: $otp\n\nIt expires in 15 minutes."
        )
    }

    fun login(emailInput: String, passwordInput: String): Pair<String, String>? {
        val userRow = userRepository.findByEmail(emailInput) ?: return null

        val isVerified = userRow[Users.isVerified]
        if (!isVerified) {
            throw IllegalArgumentException("EMAIL_NOT_VERIFIED")
        }

        val savedHash = userRow[Users.passwordHash]
        val userId = userRow[Users.id]

        if (PasswordUtils.verifyPassword(passwordInput, savedHash)) {
            val accessToken = JwtUtils.generateAccessToken(userId, emailInput)
            val refreshToken = JwtUtils.generateRefreshToken(userId)
            return Pair(accessToken, refreshToken)
        }

        return null
    }

    suspend fun verifyEmail(email: String, otp: String): Pair<String, String>? {
        val otpRow = otpRepository.findLatestOtp(email, "verification") ?: throw IllegalArgumentException("INVALID_OTP")
        val expiresAt = otpRow[Otps.expiresAt]
        if (LocalDateTime.now().isAfter(expiresAt)) {
            throw IllegalArgumentException("EXPIRED_OTP")
        }
        val savedHash = otpRow[Otps.otpHash]
        if (!PasswordUtils.verifyPassword(otp, savedHash)) {
            throw IllegalArgumentException("INVALID_OTP")
        }
        
        userRepository.verifyUser(email)
        otpRepository.deleteOtp(email, "verification")
        
        val userRow = userRepository.findByEmail(email) ?: return null
        val userId = userRow[Users.id]
        
        val accessToken = JwtUtils.generateAccessToken(userId, email)
        val refreshToken = JwtUtils.generateRefreshToken(userId)
        return Pair(accessToken, refreshToken)
    }

    suspend fun sendResetPasswordOtp(email: String) {
        userRepository.findByEmail(email) ?: throw IllegalArgumentException("USER_NOT_FOUND")
        
        val otp = generateOtp()
        val hashedOtp = PasswordUtils.hashPassword(otp)
        val expiresAt = LocalDateTime.now().plusMinutes(15)
        otpRepository.insertOtp(email, hashedOtp, expiresAt, "reset")
        
        emailService.sendEmail(
            toEmail = email,
            subject = "Reset your StudySync Password",
            body = "Your password reset code is: $otp\n\nIt expires in 15 minutes."
        )
    }

    suspend fun resetPassword(email: String, otp: String, newPassword: String) {
        val otpRow = otpRepository.findLatestOtp(email, "reset") ?: throw IllegalArgumentException("INVALID_OTP")
        val expiresAt = otpRow[Otps.expiresAt]
        if (LocalDateTime.now().isAfter(expiresAt)) {
            throw IllegalArgumentException("EXPIRED_OTP")
        }
        val savedHash = otpRow[Otps.otpHash]
        if (!PasswordUtils.verifyPassword(otp, savedHash)) {
            throw IllegalArgumentException("INVALID_OTP")
        }
        
        val newHash = PasswordUtils.hashPassword(newPassword)
        userRepository.updatePassword(email, newHash)
        otpRepository.deleteOtp(email, "reset")
    }

    fun refresh(refreshTokenInput: String): Pair<String, String>? {
        val decoded = JwtUtils.verifyToken(refreshTokenInput) ?: return null
        val userIdString = decoded.getClaim("userId").asString() ?: return null
        val userId = UUID.fromString(userIdString)

        val userRow = userRepository.findById(userId) ?: return null
        val email = userRow[Users.email]

        val newAccessToken = JwtUtils.generateAccessToken(userId, email)
        val newRefreshToken = JwtUtils.generateRefreshToken(userId)

        return Pair(newAccessToken, newRefreshToken)
    }
}