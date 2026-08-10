package com.example.studysyncandroid.data.repository

import com.example.studysyncandroid.data.remote.AnalyticsApi
import com.example.studysyncandroid.data.remote.dto.DailyRetentionDto
import javax.inject.Inject

class AnalyticsRepository @Inject constructor(
    private val analyticsApi: AnalyticsApi
) {
    suspend fun getRetentionCurve(): Result<List<DailyRetentionDto>> = runCatching {
        analyticsApi.getRetentionCurve()
    }
}
