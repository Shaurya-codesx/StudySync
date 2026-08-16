package com.example.studysyncandroid.data.repository

import com.example.studysyncandroid.data.local.dao.CardDao
import com.example.studysyncandroid.data.local.entities.CardEntity
import com.example.studysyncandroid.data.remote.CardApi
import com.example.studysyncandroid.data.remote.dto.ReviewCardRequest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

open class CardRepository @Inject constructor(
    private val cardApi: CardApi,
    private val cardDao: CardDao
) {
    /**
     * Fetches only the cards for a specific deck that are due for review right now.
     */
    /**
     * Fetches only the cards for a specific deck that are due for review right now.
     */
    open suspend fun getDueCards(deckId: String): List<CardEntity> {
        // 1. Sync cards from the backend first to ensure Room is up to date
        // runCatching silently swallows network errors so offline mode still works
        runCatching {
            val remoteCards = cardApi.getCardsForDeck(deckId)
            cardDao.insertCards(
                remoteCards.map { card ->
                    CardEntity(
                        id = card.id,
                        deckId = deckId,
                        question = card.question,
                        answer = card.answer,
                        dueDate = card.dueDate
                    )
                }
            )
        }

        // 2. Query the local Room DB for cards that are due
        return cardDao.getDueCards(deckId, nowIso())
    }

    /**
     * Submits a quality rating for a card to the backend, then updates the local cache
     * so it doesn't show up in the due queue until its new due date.
     */
    suspend fun reviewCard(cardId: String, quality: Int): Result<Unit> = runCatching {
        // 1. Send the review to the server using the DTO we made in Step 1
        val response = cardApi.reviewCard(cardId, ReviewCardRequest(quality))

        // 2. Update the local cache with the new due date
        cardDao.updateCardDueDate(cardId, response.dueDate)
    }

    private fun nowIso(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        return formatter.format(Date())
    }
}