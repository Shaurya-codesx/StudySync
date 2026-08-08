package com.example.dto

import kotlinx.serialization.Serializable

@Serializable
data class DeckResponse(
    val id: String,
    val folderId: String? = null,
    val title: String,
    val cardCount: Int = 0,
    val createdAt: String
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