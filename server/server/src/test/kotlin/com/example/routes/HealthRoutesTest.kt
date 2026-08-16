package com.example.routes

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class HealthRoutesTest {

    @Test
    fun testHealthEndpoint() = testApplication {
        client.get("/health").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("{\"status\":\"yo\"}", bodyAsText())
        }
    }
}
