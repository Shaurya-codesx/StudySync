package com.example.studysyncandroid.data.remote

import com.example.studysyncandroid.data.remote.dto.CardResponse
import com.example.studysyncandroid.data.remote.dto.ReviewCardRequest
import com.example.studysyncandroid.data.remote.dto.ReviewCardResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import javax.inject.Inject

class CardApi @Inject constructor(
    private val client: HttpClient
) {
    suspend fun reviewCard(cardId: String, request: ReviewCardRequest): ReviewCardResponse =
        client.post("/cards/$cardId/review") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

    suspend fun getCardsForDeck(deckId: String): List<CardResponse> =
        client.get("/decks/$deckId/cards").body()
}