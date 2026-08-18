package com.example.studysyncandroid.ui.review

import com.example.studysyncandroid.data.local.entities.CardEntity
import com.example.studysyncandroid.data.local.entities.DeckEntity
import com.example.studysyncandroid.data.remote.dto.ReviewCardResponse
import com.example.studysyncandroid.data.repository.CardRepository
import com.example.studysyncandroid.data.repository.DeckRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class ReviewViewModelTest {

    private lateinit var cardRepository: CardRepository
    private lateinit var deckRepository: DeckRepository
    private lateinit var viewModel: ReviewViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        cardRepository = mock()
        deckRepository = mock()
        
        viewModel = ReviewViewModel(cardRepository, deckRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadDueCards fetches deck title and due cards`() = runBlocking {
        val testDeckId = "deck123"
        val mockDecks = listOf(
            DeckEntity("deck123", "folder1", "Test Deck", 10, false, "now")
        )
        val mockCards = listOf(
            CardEntity("card1", "deck123", "Q1", "A1", "now"),
            CardEntity("card2", "deck123", "Q2", "A2", "now")
        )

        whenever(deckRepository.getDecksStream()).thenReturn(flowOf(mockDecks))
        whenever(cardRepository.getDueCards(testDeckId)).thenReturn(mockCards)

        viewModel.loadDueCards(testDeckId)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Test Deck", state.deckTitle)
        assertEquals(2, state.dueCards.size)
        assertEquals(0, state.currentCardIndex)
        assertFalse(state.isAnswerRevealed)
        assertNull(state.error)
        assertFalse(state.hasNoDueCards)
        assertFalse(state.isReviewFinished)
        assertEquals("card1", state.currentCard?.id)
    }

    @Test
    fun `loadDueCards with no due cards sets hasNoDueCards`() = runBlocking {
        val testDeckId = "deck123"
        val mockDecks = listOf(
            DeckEntity("deck123", "folder1", "Test Deck", 10, false, "now")
        )

        whenever(deckRepository.getDecksStream()).thenReturn(flowOf(mockDecks))
        whenever(cardRepository.getDueCards(testDeckId)).thenReturn(emptyList())

        viewModel.loadDueCards(testDeckId)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.dueCards.isEmpty())
        assertTrue(state.hasNoDueCards)
    }

    @Test
    fun `revealAnswer sets isAnswerRevealed to true`() = runBlocking {
        // Pre-populate with a card
        val mockCards = listOf(
            CardEntity("card1", "deck123", "Q1", "A1", "now")
        )
        whenever(deckRepository.getDecksStream()).thenReturn(flowOf(emptyList()))
        whenever(cardRepository.getDueCards("deck123")).thenReturn(mockCards)
        
        viewModel.loadDueCards("deck123")
        assertFalse(viewModel.uiState.value.isAnswerRevealed)

        viewModel.revealAnswer()

        assertTrue(viewModel.uiState.value.isAnswerRevealed)
    }

    @Test
    fun `submitRating on success advances to next card and hides answer`() = runBlocking {
        val mockCards = listOf(
            CardEntity("card1", "deck123", "Q1", "A1", "now"),
            CardEntity("card2", "deck123", "Q2", "A2", "now")
        )
        whenever(deckRepository.getDecksStream()).thenReturn(flowOf(emptyList()))
        whenever(cardRepository.getDueCards("deck123")).thenReturn(mockCards)
        
        // Mock successful submission
        whenever(cardRepository.reviewCard("card1", 3)).thenReturn(
            Result.success(Unit)
        )

        viewModel.loadDueCards("deck123")
        viewModel.revealAnswer()
        
        // Ensure state before submission
        assertEquals(0, viewModel.uiState.value.currentCardIndex)
        assertTrue(viewModel.uiState.value.isAnswerRevealed)

        viewModel.submitRating(3)

        verify(cardRepository).reviewCard("card1", 3)
        
        val state = viewModel.uiState.value
        assertEquals(1, state.currentCardIndex)
        assertEquals("card2", state.currentCard?.id)
        assertFalse(state.isAnswerRevealed)
        assertFalse(state.isReviewFinished)
    }

    @Test
    fun `submitRating on failure sets error state`() = runBlocking {
        val mockCards = listOf(
            CardEntity("card1", "deck123", "Q1", "A1", "now")
        )
        whenever(deckRepository.getDecksStream()).thenReturn(flowOf(emptyList()))
        whenever(cardRepository.getDueCards("deck123")).thenReturn(mockCards)
        
        // Mock failed submission
        whenever(cardRepository.reviewCard("card1", 1)).thenReturn(
            Result.failure(Exception("Network Error"))
        )

        viewModel.loadDueCards("deck123")
        viewModel.submitRating(1)

        val state = viewModel.uiState.value
        assertEquals(0, state.currentCardIndex) // Should not advance
        assertEquals("Failed to submit review. Check connection.", state.error)
    }

    @Test
    fun `completing all cards sets isReviewFinished`() = runBlocking {
        val mockCards = listOf(
            CardEntity("card1", "deck123", "Q1", "A1", "now")
        )
        whenever(deckRepository.getDecksStream()).thenReturn(flowOf(emptyList()))
        whenever(cardRepository.getDueCards("deck123")).thenReturn(mockCards)
        
        whenever(cardRepository.reviewCard("card1", 4)).thenReturn(
            Result.success(Unit)
        )

        viewModel.loadDueCards("deck123")
        viewModel.submitRating(4)

        val state = viewModel.uiState.value
        assertEquals(1, state.currentCardIndex)
        assertTrue(state.isReviewFinished)
        assertNull(state.currentCard)
    }
}
