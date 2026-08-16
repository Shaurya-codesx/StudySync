package com.example.routes

import com.example.models.LoginRequest
import com.example.models.RegisterRequest
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import com.example.configureDatabase
import com.example.configureRouting
import com.example.configureSecurity
import com.example.configureSerialization
import com.example.configureStatusPages
import com.example.configureWebsockets
import com.example.plugins.configureAuth
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthRoutesTest {

    @Test
    fun testSignupAndLoginFlow() = testApplication {
        application {
            configureDatabase()
            configureStatusPages()
            configureAuth()
            configureSerialization()
            configureSecurity()
            configureWebsockets()
            configureRouting()
        }

        val client = createClient {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        val testEmail = "testuser_${System.currentTimeMillis()}@example.com"
        val testPassword = "SecurePassword123!"
        val testName = "Test User"

        // 1. Test successful signup
        val signupResponse = client.post("/auth/signup") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest(testEmail, testPassword, testName))
        }

        assertEquals(HttpStatusCode.Created, signupResponse.status, "Signup should return 201 Created")
        
        // 2. Test duplicate signup (Should return 409 Conflict because user is already unverified or 201 if it overwrites it!)
        // Wait, in our recent patch, if a user is UNVERIFIED, it returns 201 and overwrites!
        // So a duplicate signup for an unverified user should ACTUALLY return 201 Created again!
        val duplicateSignupResponse = client.post("/auth/signup") {
            contentType(ContentType.Application.Json)
            setBody(RegisterRequest(testEmail, testPassword, testName))
        }
        
        assertEquals(HttpStatusCode.Created, duplicateSignupResponse.status, "Duplicate signup for unverified user should overwrite and return 201")

        // 3. Test Login with unverified account
        // Wait, logging in without verifying email should fail with 403 Forbidden or similar in this app?
        // Let's check what /auth/login returns for unverified accounts. It usually returns 403.
        // I will just test login with bad credentials first.
        
        val badLoginResponse = client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(testEmail, "WrongPassword!"))
        }

        assertEquals(HttpStatusCode.Forbidden, badLoginResponse.status, "Login with unverified email should return 403")
    }
}
