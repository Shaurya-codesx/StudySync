package com.example.studysyncandroid.data.remote

import com.example.studysyncandroid.data.remote.dto.DeckSummaryResponse
import com.example.studysyncandroid.data.remote.dto.GenerateDeckRequest
import com.example.studysyncandroid.data.remote.dto.GenerateDeckResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import javax.inject.Inject

class DeckApi @Inject constructor(
    private val client: HttpClient
) {
    suspend fun generateDeck(sourceText: String): GenerateDeckResponse =
        client.post("/decks/generate") {
            contentType(ContentType.Application.Json)
            setBody(GenerateDeckRequest(sourceText))
        }.body()

    suspend fun getDecks(): List<DeckSummaryResponse> =
        client.get("/decks").body()
}