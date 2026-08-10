package com.example.services

import com.example.dto.DailyRetentionDto
import com.example.repositories.AnalyticsRepository
import java.util.UUID

class AnalyticsService(private val analyticsRepository: AnalyticsRepository) {
    fun getRetentionCurve(userId: UUID): List<DailyRetentionDto> {
        return analyticsRepository.getRetentionCurve(userId)
    }

    fun getLibraryStatus(userId: UUID): com.example.dto.LibraryStatusDto {
        return analyticsRepository.getLibraryStatus(userId)
    }

    fun getUpcomingReviews(userId: UUID): List<com.example.dto.UpcomingReviewDto> {
        return analyticsRepository.getUpcomingReviews(userId)
    }
}
