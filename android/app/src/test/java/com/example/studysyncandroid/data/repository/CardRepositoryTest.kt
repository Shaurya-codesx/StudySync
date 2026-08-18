package com.example.studysyncandroid.data.repository

import com.example.studysyncandroid.data.local.dao.CardDao
import com.example.studysyncandroid.data.local.entities.CardEntity
import com.example.studysyncandroid.data.remote.CardApi
import com.example.studysyncandroid.data.remote.dto.CardResponse
import com.example.studysyncandroid.data.remote.dto.ReviewCardRequest
import com.example.studysyncandroid.data.remote.dto.ReviewCardResponse
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class CardRepositoryTest {

    private lateinit var cardApi: CardApi
    private lateinit var cardDao: CardDao
    private lateinit var repository: CardRepository

    @Before
    fun setup() {
        cardApi = mock()
        cardDao = mock()
        repository = CardRepository(cardApi, cardDao)
    }

    @Test
    fun `getDueCards online successfully syncs with network and queries DB`() = runBlocking {
        val deckId = "deck123"
        val remoteCards = listOf(
            CardResponse("card1", "Q1", "A1", "future")
        )
        val localDueCards = listOf(
            CardEntity("card1", deckId, "Q1", "A1", "past")
        )

        whenever(cardApi.getCardsForDeck(deckId)).thenReturn(remoteCards)
        whenever(cardDao.getDueCards(eq(deckId), any())).thenReturn(localDueCards)

        val result = repository.getDueCards(deckId)

        // Verify API was called
        verify(cardApi).getCardsForDeck(deckId)

        // Verify insertion of mapped DTOs
        verify(cardDao).insertCards(argThat {
            size == 1 &&
            get(0).id == "card1" &&
            get(0).deckId == deckId &&
            get(0).dueDate == "future"
        })

        // Verify local query was executed
        verify(cardDao).getDueCards(eq(deckId), any())
        assertEquals(1, result.size)
        assertEquals("card1", result[0].id)
    }

    @Test
    fun `getDueCards offline silently swallows exception and queries DB`() = runBlocking {
        val deckId = "deck123"
        val localDueCards = listOf(
            CardEntity("card1", deckId, "Q1", "A1", "past")
        )

        // Simulate network failure
        whenever(cardApi.getCardsForDeck(deckId)).thenThrow(RuntimeException("No internet"))
        whenever(cardDao.getDueCards(eq(deckId), any())).thenReturn(localDueCards)

        val result = repository.getDueCards(deckId)

        // Verify we still got cards from the local database despite network crash
        verify(cardDao, never()).insertCards(any())
        verify(cardDao).getDueCards(eq(deckId), any())
        
        assertEquals(1, result.size)
        assertEquals("card1", result[0].id)
    }

    @Test
    fun `reviewCard success pushes to server and write-through updates local DB`() = runBlocking {
        val cardId = "card1"
        val request = ReviewCardRequest(3)
        val response = ReviewCardResponse("card1", 2.5, 3, "2024-01-01T00:00:00Z")

        whenever(cardApi.reviewCard(cardId, request)).thenReturn(response)

        val result = repository.reviewCard(cardId, 3)

        // Verify network call was made
        verify(cardApi).reviewCard(cardId, request)
        
        // Verify local DB was updated with new date (write-through cache)
        verify(cardDao).updateCardDueDate(cardId, "2024-01-01T00:00:00Z")
        
        assertTrue(result.isSuccess)
    }

    @Test
    fun `reviewCard failure does not update local DB`() = runBlocking {
        val cardId = "card1"
        val request = ReviewCardRequest(3)

        whenever(cardApi.reviewCard(cardId, request)).thenThrow(RuntimeException("Network Error"))

        val result = repository.reviewCard(cardId, 3)

        verify(cardApi).reviewCard(cardId, request)
        
        // Verify we don't update local state if server rejected the rating
        verify(cardDao, never()).updateCardDueDate(any(), any())
        
        assertTrue(result.isFailure)
    }
}
