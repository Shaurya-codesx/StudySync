package com.example.repositories

import com.example.dto.DailyRetentionDto
import com.example.models.Cards
import com.example.models.Decks
import com.example.models.ReviewLogs
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

class AnalyticsRepository {

    fun getRetentionCurve(userId: UUID): List<DailyRetentionDto> = transaction {
        val today = LocalDate.now()
        val startDate = today.minusDays(29) // 30 days including today
        val startDateTime = startDate.atStartOfDay()

        // 1. Fetch raw logs in the last 30 days
        val rawLogs = (ReviewLogs innerJoin Cards innerJoin Decks)
            .slice(ReviewLogs.quality, ReviewLogs.reviewedAt)
            .select { (Decks.userId eq userId) and (ReviewLogs.reviewedAt greaterEq startDateTime) }
            .toList()

        // Group the logs by YYYY-MM-DD
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        val logsByDate = rawLogs.groupBy {
            it[ReviewLogs.reviewedAt].toLocalDate().format(formatter)
        }

        // 2. Generate 30 days array
        val results = mutableListOf<DailyRetentionDto>()
        for (i in 0L..29L) {
            val dateObj = startDate.plusDays(i)
            val dateStr = dateObj.format(formatter)
            
            val dailyLogs = logsByDate[dateStr]
            
            if (dailyLogs == null || dailyLogs.isEmpty()) {
                results.add(DailyRetentionDto(date = dateStr, retentionPercentage = null, totalReviews = 0))
            } else {
                val total = dailyLogs.size
                val passed = dailyLogs.count { it[ReviewLogs.quality] >= 3 }
                val percentage = passed.toFloat() / total
                results.add(DailyRetentionDto(date = dateStr, retentionPercentage = percentage, totalReviews = total))
            }
        }

        results
    }
}
