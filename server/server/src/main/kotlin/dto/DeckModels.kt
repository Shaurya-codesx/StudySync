package com.example.dto

import kotlinx.serialization.Serializable

@Serializable
data class DeckResponse(
    val id: String,
    val folderId: String? = null,
    val title: String,
    val cardCount: Int = 0,
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
data class DeckDetailResponse(
    val id: String,
    val folderId: String? = null,
    val title: String,
    val sourceText: String?,
    val createdAt: String
)

@Serializable
data class DeckCreateRequest(
    val title: String,
    val folderId: String? = null
)

@Serializable
data class UpdateDeckRequest(
    val folderId: String? = null
)