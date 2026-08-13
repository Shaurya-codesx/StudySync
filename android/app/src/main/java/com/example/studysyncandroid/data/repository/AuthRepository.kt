package com.example.studysyncandroid.data.repository

import com.example.studysyncandroid.data.local.AppDatabase
import com.example.studysyncandroid.data.local.TokenDataStore
import com.example.studysyncandroid.data.remote.AuthApi
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.clearAuthTokens
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val tokenDataStore: TokenDataStore,
    private val appDatabase: AppDatabase,
    private val httpClient: HttpClient
) {
    suspend fun signup(email: String, password: String, displayName: String): Result<Unit> =
        runCatching {
            authApi.signup(email, password, displayName)
            Unit
        }

    suspend fun login(email: String, password: String): Result<Unit> =
        runCatching {
            val response = authApi.login(email, password)
            tokenDataStore.saveTokens(response.accessToken, response.refreshToken)
            // Force Ktor's Auth plugin to drop any token cached from a
            // previous session in this same app process and re-read from
            // DataStore on the next request.
            httpClient.clearAuthTokens()
        }

    suspend fun logout() {
        tokenDataStore.clearTokens()
        httpClient.clearAuthTokens()
        withContext(Dispatchers.IO) {
            appDatabase.clearAllTables()
        }
    }

    suspend fun isLoggedIn(): Boolean = tokenDataStore.hasValidSession()

    suspend fun verifyEmail(email: String, otp: String): Result<Unit> =
        runCatching {
            val response = authApi.verifyEmail(email, otp)
            tokenDataStore.saveTokens(response.accessToken, response.refreshToken)
            httpClient.clearAuthTokens()
        }

    suspend fun resendVerification(email: String): Result<Unit> =
        runCatching {
            authApi.resendVerification(email)
            Unit
        }

    suspend fun forgotPassword(email: String): Result<Unit> =
        runCatching {
            authApi.forgotPassword(email)
            Unit
        }

    suspend fun resetPassword(email: String, otp: String, newPassword: String): Result<Unit> =
        runCatching {
            authApi.resetPassword(email, otp, newPassword)
            Unit
        }
}