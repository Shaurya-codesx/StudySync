package com.example.studysyncandroid.data.repository

import com.example.studysyncandroid.data.local.dao.CardDao
import com.example.studysyncandroid.data.local.dao.DeckDao
import com.example.studysyncandroid.data.remote.DeckApi
import com.example.studysyncandroid.data.remote.dto.DeckSummaryResponse
import com.example.studysyncandroid.data.remote.dto.GenerateDeckResponse
import com.example.studysyncandroid.data.remote.dto.GeneratedCardDto
import com.example.studysyncandroid.data.remote.dto.UpdateDeckRequest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class DeckRepositoryTest {

    private lateinit var deckApi: DeckApi
    private lateinit var deckDao: DeckDao
    private lateinit var cardDao: CardDao
    private lateinit var repository: DeckRepository

    @Before
    fun setup() {
        deckApi = mock()
        deckDao = mock()
        cardDao = mock()
        repository = DeckRepository(deckApi, deckDao, cardDao)
    }

    @Test
    fun `generateDeck success parses DTO and inserts into local DB`() = runBlocking {
        val sourceText = "Some textbook content"
        val mockCards = listOf(
            GeneratedCardDto("card1", "Q1", "A1"),
            GeneratedCardDto("card2", "Q2", "A2")
        )
        val response = GenerateDeckResponse("deck123", "Science Deck", mockCards)

        whenever(deckApi.generateDeck(sourceText)).thenReturn(response)

        val result = repository.generateDeck(sourceText)

        verify(deckApi).generateDeck(sourceText)

        // Verify it maps and inserts the DeckEntity
        verify(deckDao).insertDeck(argThat {
            id == "deck123" && title == "Science Deck" && cardCount == 2
        })

        // Verify it maps and inserts the 2 CardEntities
        verify(cardDao).insertCards(argThat {
            size == 2 && 
            get(0).id == "card1" && get(0).deckId == "deck123" && get(0).question == "Q1" &&
            get(1).id == "card2" && get(1).deckId == "deck123" && get(1).question == "Q2"
        })

        assertTrue(result.isSuccess)
        assertEquals("deck123", result.getOrNull())
    }

    @Test
    fun `generateDeck failure skips DB insertion`() = runBlocking {
        whenever(deckApi.generateDeck(any())).thenThrow(RuntimeException("AI error"))

        val result = repository.generateDeck("text")

        verify(deckDao, never()).insertDeck(any())
        verify(cardDao, never()).insertCards(any())

        assertTrue(result.isFailure)
    }

    @Test
    fun `refreshDecks success parses and syncs all decks to DB`() = runBlocking {
        val remoteDecks = listOf(
            DeckSummaryResponse("deck1", "folder1", "Deck 1", 10, true, "date1"),
            DeckSummaryResponse("deck2", null, "Deck 2", 5, false, "date2")
        )

        whenever(deckApi.getDecks()).thenReturn(remoteDecks)

        val result = repository.refreshDecks()

        verify(deckApi).getDecks()
        verify(deckDao).insertDecks(argThat {
            size == 2 &&
            get(0).id == "deck1" && get(0).folderId == "folder1" &&
            get(1).id == "deck2" && get(1).folderId == null
        })

        assertTrue(result.isSuccess)
    }

    @Test
    fun `moveDeckToFolder success pushes to server and updates local DB`() = runBlocking {
        whenever(deckApi.patchDeckFolder(any(), any())).thenReturn(Unit)

        val result = repository.moveDeckToFolder("deck123", "folder456")

        verify(deckApi).patchDeckFolder("deck123", UpdateDeckRequest("folder456"))
        verify(deckDao).updateDeckFolder("deck123", "folder456")
        
        assertTrue(result.isSuccess)
    }

    @Test
    fun `moveDeckToFolder failure prevents local DB update`() = runBlocking {
        whenever(deckApi.patchDeckFolder(any(), any())).thenThrow(RuntimeException("Network Error"))

        val result = repository.moveDeckToFolder("deck123", "folder456")

        verify(deckDao, never()).updateDeckFolder(any(), any())
        assertTrue(result.isFailure)
    }

    @Test
    fun `deleteDeck success pushes to server and deletes from local DB`() = runBlocking {
        whenever(deckApi.deleteDeck("deck123")).thenReturn(Unit)

        val result = repository.deleteDeck("deck123")

        verify(deckApi).deleteDeck("deck123")
        verify(deckDao).deleteDeckById("deck123")
        
        assertTrue(result.isSuccess)
    }
}
