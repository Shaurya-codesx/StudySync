package com.example.studysyncandroid.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.studysyncandroid.data.local.dao.CardDao
import com.example.studysyncandroid.data.local.dao.DeckDao
import com.example.studysyncandroid.data.local.entities.CardEntity
import com.example.studysyncandroid.data.local.entities.DeckEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {

    private lateinit var db: AppDatabase
    private lateinit var deckDao: DeckDao
    private lateinit var cardDao: CardDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // Use an in-memory database so information is not persisted between tests
        db = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java
        ).allowMainThreadQueries().build() // Allowed for testing
        
        deckDao = db.deckDao()
        cardDao = db.cardDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun dateFiltering_getDueCards_returnsOnlyPastAndPresentCards() = runBlocking {
        // Setup a deck
        val deck = DeckEntity("deck1", null, "Science", 3, false, "2024-01-01T00:00:00Z")
        deckDao.insertDeck(deck)

        // Insert 3 cards with different due dates
        val cardPast = CardEntity("card_past", "deck1", "Q1", "A1", "2024-05-01T10:00:00Z")
        val cardPresent = CardEntity("card_present", "deck1", "Q2", "A2", "2024-05-01T12:00:00Z")
        val cardFuture = CardEntity("card_future", "deck1", "Q3", "A3", "2024-05-01T14:00:00Z")
        
        cardDao.insertCards(listOf(cardPast, cardPresent, cardFuture))

        // We simulate "now" as exactly 12:00:00Z
        val simulatedNow = "2024-05-01T12:00:00Z"
        
        // Execute the custom query
        val dueCards = cardDao.getDueCards("deck1", simulatedNow)

        // It should ONLY return the past card and the present card. Future card is excluded.
        assertEquals(2, dueCards.size)
        val dueIds = dueCards.map { it.id }
        assertTrue(dueIds.contains("card_past"))
        assertTrue(dueIds.contains("card_present"))
        assertTrue(!dueIds.contains("card_future"))
    }

    @Test
    fun foreignKeyCascade_deletingDeck_wipesAllChildCards() = runBlocking {
        // Insert a deck and 5 child cards
        val deck = DeckEntity("deck_to_delete", null, "Math", 5, false, "2024-01-01T00:00:00Z")
        deckDao.insertDeck(deck)

        val cards = (1..5).map { i ->
            CardEntity("card_$i", "deck_to_delete", "Q", "A", "2024-01-01T00:00:00Z")
        }
        cardDao.insertCards(cards)

        // Verify they are inserted
        var currentCards = cardDao.getCardsForDeck("deck_to_delete").first()
        assertEquals(5, currentCards.size)

        // Act: Delete the deck
        deckDao.deleteDeckById("deck_to_delete")

        // Assert: All child cards should be wiped automatically by CASCADE
        currentCards = cardDao.getCardsForDeck("deck_to_delete").first()
        assertEquals(0, currentCards.size)
    }

    @Test
    fun conflictStrategy_insertingExistingCard_replacesInsteadOfCrashing() = runBlocking {
        val deck = DeckEntity("deck_upsert", null, "History", 1, false, "2024-01-01T00:00:00Z")
        deckDao.insertDeck(deck)

        // Insert initial card
        val initialCard = CardEntity("card_1", "deck_upsert", "Q_old", "A_old", "2024-01-01")
        cardDao.insertCard(initialCard)

        // Simulate a network sync pulling down the same card ID but with new data
        val updatedCard = CardEntity("card_1", "deck_upsert", "Q_new", "A_new", "2025-01-01")
        cardDao.insertCard(updatedCard)

        // Query the DB
        val cardsInDb = cardDao.getCardsForDeck("deck_upsert").first()

        // Size should still be 1 (upserted, not duplicated)
        assertEquals(1, cardsInDb.size)
        
        // Data should be overwritten with the new values
        assertEquals("Q_new", cardsInDb[0].question)
        assertEquals("A_new", cardsInDb[0].answer)
        assertEquals("2025-01-01", cardsInDb[0].dueDate)
    }
}
