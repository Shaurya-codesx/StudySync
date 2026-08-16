package com.example.routes

import com.example.configureDatabase
import com.example.configureRouting
import com.example.configureSecurity
import com.example.configureSerialization
import com.example.configureStatusPages
import com.example.configureWebsockets
import com.example.dto.DeckCreateRequest
import com.example.models.Decks
import com.example.models.Users
import com.example.plugins.configureAuth
import com.example.utils.JwtUtils
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class DeckRoutesTest {

    @Test
    fun testDeckCreationAndAuthorization() = testApplication {
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
            install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true })
            }
        }

        var userAToken: String = ""
        var userBToken: String = ""
        
        // Force the Ktor application to boot up so Database.connect() is called
        client.get("/health")

        // 1. Setup Test Data (Insert two distinct users directly into the DB and generate their JWTs)
        transaction {
            Decks.deleteAll()
            Users.deleteAll()

            val userAId = Users.insert {
                it[email] = "usera@example.com"
                it[passwordHash] = "hashed_pw"
                it[isVerified] = true
            }[Users.id]

            val userBId = Users.insert {
                it[email] = "userb@example.com"
                it[passwordHash] = "hashed_pw"
                it[isVerified] = true
            }[Users.id]

            userAToken = JwtUtils.generateAccessToken(userAId, "usera@example.com")
            userBToken = JwtUtils.generateAccessToken(userBId, "userb@example.com")
        }

        // 2. Test Deck Creation by User A (CRUD functionality)
        val createResponse = client.post("/decks") {
            header(HttpHeaders.Authorization, "Bearer $userAToken")
            contentType(ContentType.Application.Json)
            setBody(DeckCreateRequest(title = "My Test Deck"))
        }

        assertEquals(HttpStatusCode.Created, createResponse.status, "User A should successfully create a deck")
        val responseBody = createResponse.bodyAsText()
        val jsonElement = Json.parseToJsonElement(responseBody).jsonObject
        val createdDeckId = jsonElement["id"]?.jsonPrimitive?.content
        
        assertNotNull(createdDeckId, "Created Deck ID should not be null")
        assertEquals("My Test Deck", jsonElement["title"]?.jsonPrimitive?.content)

        // 3. Test Fetching the Deck by User A (Owner)
        val getResponseA = client.get("/decks/$createdDeckId") {
            header(HttpHeaders.Authorization, "Bearer $userAToken")
        }
        assertEquals(HttpStatusCode.OK, getResponseA.status, "User A should be able to fetch their own deck")

        // 4. Test Security/Authorization: User B tries to fetch User A's deck
        val getResponseB = client.get("/decks/$createdDeckId") {
            header(HttpHeaders.Authorization, "Bearer $userBToken")
        }
        assertEquals(HttpStatusCode.NotFound, getResponseB.status, "User B should NOT be able to fetch User A's deck")
        
        // 5. Test Deletion Security: User B tries to delete User A's deck
        val deleteResponseB = client.delete("/decks/$createdDeckId") {
            header(HttpHeaders.Authorization, "Bearer $userBToken")
        }
        assertEquals(HttpStatusCode.NotFound, deleteResponseB.status, "User B should NOT be able to delete User A's deck")
        
        // 6. Test Deletion by User A (Owner)
        val deleteResponseA = client.delete("/decks/$createdDeckId") {
            header(HttpHeaders.Authorization, "Bearer $userAToken")
        }
        assertEquals(HttpStatusCode.NoContent, deleteResponseA.status, "User A should successfully delete their deck")
    }
}
