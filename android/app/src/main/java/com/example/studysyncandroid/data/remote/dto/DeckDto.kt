package com.example.studysyncandroid.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class GenerateDeckRequest(
    val sourceText: String
)

@Serializable
data class GeneratedCardDto(
    val id: String,
    val question: String,
    val answer: String
)

@Serializable
data class GenerateDeckResponse(
    val deckId: String,
    val title: String,
    val cards: List<GeneratedCardDto>
)

@Serializable
data class DeckSummaryResponse(
    val id: String,
    val title: String,
    val cardCount: Int,
    val createdAt: String
)