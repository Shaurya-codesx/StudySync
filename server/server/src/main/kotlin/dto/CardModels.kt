package com.example.dto

import kotlinx.serialization.Serializable

@Serializable
data class CardResponse(
    val id: String,
    val question: String,
    val answer: String,
    val dueDate: String
)