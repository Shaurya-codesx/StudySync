package com.example.studysyncandroid.data.repository

import com.example.studysyncandroid.data.local.dao.CardDao
import com.example.studysyncandroid.data.local.dao.DeckDao
import com.example.studysyncandroid.data.local.entities.CardEntity
import com.example.studysyncandroid.data.local.entities.DeckEntity
import com.example.studysyncandroid.data.remote.DeckApi
import com.example.studysyncandroid.data.remote.dto.UpdateDeckRequest
import kotlinx.coroutines.flow.Flow
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.text.SimpleDateFormat
import javax.inject.Inject

class DeckRepository @Inject constructor(
    private val deckApi: DeckApi,
    private val deckDao: DeckDao,
    private val cardDao: CardDao
) {
    fun getDecksStream(): Flow<List<DeckEntity>> = deckDao.getAllDecks()

    suspend fun generateDeck(sourceText: String): Result<String> = runCatching {
        val response = deckApi.generateDeck(sourceText)
        val now = nowIso()

        deckDao.insertDeck(
            DeckEntity(
                id = response.deckId,
                title = response.title,
                cardCount = response.cards.size,
                createdAt = now
            )
        )
        cardDao.insertCards(
            response.cards.map { card ->
                CardEntity(
                    id = card.id,
                    deckId = response.deckId,
                    question = card.question,
                    answer = card.answer,
                    dueDate = now
                )
            }
        )
        response.deckId
    }

    suspend fun refreshDecks(): Result<Unit> = runCatching {
        val summaries = deckApi.getDecks()
        deckDao.insertDecks(
            summaries.map { summary ->
                DeckEntity(
                    id = summary.id,
                    folderId = summary.folderId,
                    title = summary.title,
                    cardCount = summary.cardCount,
                    createdAt = summary.createdAt
                )
            }
        )
    }

    // Placeholder timestamp for freshly generated content — the next
    // refreshDecks() call overwrites this with the server's real createdAt.
    private fun nowIso(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        return formatter.format(Date())
    }

    suspend fun moveDeckToFolder(deckId: String, folderId: String?): Result<Unit> = runCatching {
        deckApi.patchDeckFolder(deckId, UpdateDeckRequest(folderId))
        deckDao.updateDeckFolder(deckId, folderId)
    }

    suspend fun deleteDeck(deckId: String): Result<Unit> = runCatching {
        deckApi.deleteDeck(deckId)
        deckDao.deleteDeckById(deckId)
    }
}