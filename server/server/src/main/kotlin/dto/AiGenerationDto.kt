package com.example.dto

import kotlinx.serialization.Serializable

@Serializable
data class GeneratedCardDto(
    val question: String,
    val answer: String
)

@Serializable
data class AiGeneratedDeckResult(
    val title: String,
    val cards: List<GeneratedCardDto>
)