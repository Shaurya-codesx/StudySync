package com.example.studysyncandroid.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class DailyRetentionDto(
    val date: String,
    val retentionPercentage: Float?,
    val totalReviews: Int
)

@Serializable
data class LibraryStatusDto(
    val newCards: Int,
    val learningCards: Int,
    val matureCards: Int,
    val totalCards: Int
)

@Serializable
data class UpcomingReviewDto(
    val date: String,
    val cardsDue: Int
)
