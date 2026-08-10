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

    fun getLibraryStatus(userId: UUID): com.example.dto.LibraryStatusDto = transaction {
        // Fetch repetitions and intervalDays for all cards belonging to the user
        val cards = (Cards innerJoin Decks)
            .slice(Cards.repetitions, Cards.intervalDays)
            .select { Decks.userId eq userId }
            .toList()

        var newCards = 0
        var learningCards = 0
        var matureCards = 0

        for (card in cards) {
            val repetitions = card[Cards.repetitions]
            val intervalDays = card[Cards.intervalDays]

            when {
                repetitions == 0 -> newCards++
                intervalDays >= 21 -> matureCards++
                else -> learningCards++
            }
        }

        com.example.dto.LibraryStatusDto(
            newCards = newCards,
            learningCards = learningCards,
            matureCards = matureCards,
            totalCards = cards.size
        )
    }

    fun getUpcomingReviews(userId: UUID): List<com.example.dto.UpcomingReviewDto> = transaction {
        val todayObj = LocalDate.now()
        val cutoffDate = todayObj.plusDays(10)
        val cutoffDateTime = cutoffDate.atStartOfDay()

        // Fetch dueDate for all cards belonging to the user that are due before the cutoff
        val cards = (Cards innerJoin Decks)
            .slice(Cards.dueDate)
            .select { (Decks.userId eq userId) and (Cards.dueDate less cutoffDateTime) }
            .toList()

        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        
        // Initialize 10-day buckets
        val buckets = mutableMapOf<String, Int>()
        for (i in 0L..9L) {
            buckets[todayObj.plusDays(i).format(formatter)] = 0
        }

        val todayStr = todayObj.format(formatter)

        for (card in cards) {
            val dueDate = card[Cards.dueDate].toLocalDate()
            val dueDateStr = dueDate.format(formatter)

            if (dueDate.isBefore(todayObj) || dueDate.isEqual(todayObj)) {
                // Overdue or due today
                buckets[todayStr] = buckets.getOrDefault(todayStr, 0) + 1
            } else if (buckets.containsKey(dueDateStr)) {
                // Due in the future (within the 10-day window)
                buckets[dueDateStr] = buckets.getOrDefault(dueDateStr, 0) + 1
            }
        }

        // Convert back to sorted list of DTOs
        val results = mutableListOf<com.example.dto.UpcomingReviewDto>()
        for (i in 0L..9L) {
            val dateStr = todayObj.plusDays(i).format(formatter)
            results.add(com.example.dto.UpcomingReviewDto(date = dateStr, cardsDue = buckets[dateStr] ?: 0))
        }

        results
    }
}
