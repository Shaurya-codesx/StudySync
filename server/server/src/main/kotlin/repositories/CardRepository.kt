package com.example.repositories

import com.example.dto.CardResponse
import com.example.models.Cards
import com.example.models.Decks
import com.example.models.ReviewLogs
import com.example.utils.SM2Result
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

data class CardWithOwner(
    val id: UUID,
    val deckId: UUID,
    val ownerUserId: UUID,
    val easeFactor: Double,
    val intervalDays: Int,
    val repetitions: Int
)


class CardRepository {

    // Fetch all cards for a specific deck
    fun getCardsForDeck(deckId: UUID): List<CardResponse> = transaction {
        Cards.select { Cards.deckId eq deckId }
            .map {
                CardResponse(
                    id = it[Cards.id].toString(),
                    question = it[Cards.question],
                    answer = it[Cards.answer],
                    dueDate = it[Cards.dueDate].toString()
                )
            }
    }

    /** Persists the new schedule computed by SM2Calculator. */
    fun findById(cardId: UUID): CardWithOwner? = transaction {
        (Cards innerJoin Decks)
            .select { Cards.id eq cardId }
            .map {
                CardWithOwner(
                    id = it[Cards.id],
                    deckId = it[Cards.deckId],
                    ownerUserId = it[Decks.userId],
                    easeFactor = it[Cards.easeFactor].toDouble(), // Float -> Double
                    intervalDays = it[Cards.intervalDays],
                    repetitions = it[Cards.repetitions]
                )
            }
            .singleOrNull()
    }

    /** Persists the new schedule computed by SM2Calculator. */
    fun updateSchedule(cardId: UUID, result: SM2Result) = transaction {
        Cards.update({ Cards.id eq cardId }) {
            it[easeFactor] = result.easeFactor.toFloat() // Double -> Float
            it[intervalDays] = result.intervalDays
            it[repetitions] = result.repetitions
            it[dueDate] = LocalDateTime.ofInstant(result.dueDate, ZoneOffset.UTC) // Instant -> LocalDateTime (UTC)
        }
    }

    /** Logs every review attempt, regardless of pass/fail quality.
     *  reviewedAt is left unset — the column's defaultExpression(CurrentDateTime) fills it in. */
    fun insertReviewLog(cardId: UUID, quality: Int) = transaction {
        ReviewLogs.insert {
            it[id] = UUID.randomUUID()
            it[ReviewLogs.cardId] = cardId
            it[ReviewLogs.quality] = quality
        }
    }

    

}