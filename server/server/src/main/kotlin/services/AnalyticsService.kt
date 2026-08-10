package com.example.services

import com.example.dto.DailyRetentionDto
import com.example.repositories.AnalyticsRepository
import java.util.UUID

class AnalyticsService(private val analyticsRepository: AnalyticsRepository) {
    fun getRetentionCurve(userId: UUID): List<DailyRetentionDto> {
        return analyticsRepository.getRetentionCurve(userId)
    }
}
