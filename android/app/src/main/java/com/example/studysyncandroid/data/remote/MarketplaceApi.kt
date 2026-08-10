package com.example.studysyncandroid.data.remote

import com.example.studysyncandroid.data.remote.dto.DeckSummaryResponse
import com.example.studysyncandroid.data.remote.dto.PaginatedResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import javax.inject.Inject

class MarketplaceApi @Inject constructor(
    private val client: HttpClient
) {
    suspend fun getPublicDecks(page: Int, limit: Int): PaginatedResponse<DeckSummaryResponse> =
        client.get("/marketplace/decks") {
            parameter("page", page)
            parameter("limit", limit)
        }.body()

    suspend fun cloneDeck(deckId: String): DeckSummaryResponse =
        client.post("/marketplace/decks/$deckId/clone").body()
}
