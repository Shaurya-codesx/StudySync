package com.example.routes

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import com.example.configureDatabase
import com.example.configureRouting
import com.example.configureSecurity
import com.example.configureSerialization
import com.example.configureStatusPages
import com.example.configureWebsockets
import com.example.plugins.configureAuth
import kotlin.test.Test
import kotlin.test.assertEquals

class HealthRoutesTest {

    @Test
    fun testHealthEndpoint() = testApplication {
        application {
            configureDatabase()
            configureStatusPages()
            configureAuth()
            configureSerialization()
            configureSecurity()
            configureWebsockets()
            configureRouting()
        }
        client.get("/health").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("{\"status\":\"yo\"}", bodyAsText())
        }
    }
}
