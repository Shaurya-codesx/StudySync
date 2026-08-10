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

    suspend fun getLibraryStatus(): Result<com.example.studysyncandroid.data.remote.dto.LibraryStatusDto> = runCatching {
        analyticsApi.getLibraryStatus()
    }

    suspend fun getUpcomingReviews(): Result<List<com.example.studysyncandroid.data.remote.dto.UpcomingReviewDto>> = runCatching {
        analyticsApi.getUpcomingReviews()
    }
}
