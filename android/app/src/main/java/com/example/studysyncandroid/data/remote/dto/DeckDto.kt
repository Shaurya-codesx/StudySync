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
    val folderId: String? = null,
    val title: String,
    val cardCount: Int,
    val isPublic: Boolean = false,
    val createdAt: String
)

@Serializable
data class PaginatedResponse<T>(
    val items: List<T>,
    val page: Int,
    val totalPages: Int,
    val totalItems: Long
)

@Serializable
data class UpdateDeckRequest(
    val folderId: String? = null
)