package com.example.studysyncandroid.data.remote

import com.example.studysyncandroid.data.remote.dto.DailyRetentionDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class AnalyticsApi(private val client: HttpClient) {
    suspend fun getRetentionCurve(): List<DailyRetentionDto> {
        return client.get("/analytics/retention").body()
    }

    suspend fun getLibraryStatus(): com.example.studysyncandroid.data.remote.dto.LibraryStatusDto {
        return client.get("/analytics/library-status").body()
    }

    suspend fun getUpcomingReviews(): List<com.example.studysyncandroid.data.remote.dto.UpcomingReviewDto> {
        return client.get("/analytics/upcoming").body()
    }
}
