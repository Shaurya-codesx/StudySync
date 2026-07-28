package com.example.services


import com.example.repositories.CardRepository
import com.example.utils.SM2Calculator
import java.time.Instant
import java.util.UUID

/** Thrown for both "card doesn't exist" and "card belongs to someone else" —
 *  same message for both, so we don't leak which card IDs exist to non-owners. */
class CardNotFoundException(message: String = "Card not found") : Exception(message)

data class CardReviewResult(
    val id: UUID,
    val easeFactor: Double,
    val intervalDays: Int,
    val dueDate: Instant
)

class CardService(private val cardRepository: CardRepository) {

    fun reviewCard(cardId: UUID, requestingUserId: UUID, quality: Int): CardReviewResult {
        val card = cardRepository.findById(cardId) ?: throw CardNotFoundException()

        if (card.ownerUserId != requestingUserId) {
            throw CardNotFoundException()
        }

        val sm2Result = SM2Calculator.review(
            currentEaseFactor = card.easeFactor,
            currentIntervalDays = card.intervalDays,
            currentRepetitions = card.repetitions,
            quality = quality
        )

        cardRepository.updateSchedule(cardId, sm2Result)
        cardRepository.insertReviewLog(cardId, quality)

        return CardReviewResult(
            id = cardId,
            easeFactor = sm2Result.easeFactor,
            intervalDays = sm2Result.intervalDays,
            dueDate = sm2Result.dueDate
        )
    }
}