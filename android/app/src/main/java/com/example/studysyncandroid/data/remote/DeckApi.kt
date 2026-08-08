package com.example.studysyncandroid.data.remote

import com.example.studysyncandroid.data.remote.dto.DeckSummaryResponse
import com.example.studysyncandroid.data.remote.dto.GenerateDeckRequest
import com.example.studysyncandroid.data.remote.dto.GenerateDeckResponse
import com.example.studysyncandroid.data.remote.dto.UpdateDeckRequest
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
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

    suspend fun patchDeckFolder(deckId: String, request: UpdateDeckRequest) {
        client.patch("/decks/$deckId/folder") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
    }

    suspend fun deleteDeck(deckId: String) {
        client.delete("/decks/$deckId")
    }
}