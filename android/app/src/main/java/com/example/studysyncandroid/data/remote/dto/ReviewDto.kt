package com.example.studysyncandroid.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class ReviewCardRequest(
    val quality: Int
)

@Serializable
data class ReviewCardResponse(
    val id: String,
    val easeFactor: Double,
    val intervalDays: Int,
    val dueDate: String
)

@Serializable
data class CardResponse(
    val id: String,
    val question: String,
    val answer: String,
    val dueDate: String
)