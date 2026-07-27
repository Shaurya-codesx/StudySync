package com.example.dto

import kotlinx.serialization.Serializable

@Serializable
data class GenerateDeckRequest(
    val sourceText: String
)

@Serializable
data class GeneratedDeckResponse(
    val deckId: String,
    val title: String,
    val cards: List<GeneratedCardResponse>
)

@Serializable
data class GeneratedCardResponse(
    val id: String,
    val question: String,
    val answer: String
)