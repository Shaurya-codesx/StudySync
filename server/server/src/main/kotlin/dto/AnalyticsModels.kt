package com.example.dto

import kotlinx.serialization.Serializable

@Serializable
data class DailyRetentionDto(
    val date: String,
    val retentionPercentage: Float?,
    val totalReviews: Int
)
