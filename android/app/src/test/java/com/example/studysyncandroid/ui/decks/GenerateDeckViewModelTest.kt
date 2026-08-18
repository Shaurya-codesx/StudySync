package com.example.studysyncandroid.ui.decks

import com.example.studysyncandroid.data.repository.DeckRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class GenerateDeckViewModelTest {

    private lateinit var deckRepository: DeckRepository
    private lateinit var viewModel: GenerateDeckViewModel

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        deckRepository = mock()
        viewModel = GenerateDeckViewModel(deckRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Idle`() {
        val state = viewModel.uiState.value
        assertTrue(state is GenerateDeckUiState.Idle)
    }

    @Test
    fun `generateDeck on success sets Success state with deckId`() = runBlocking {
        val testSourceText = "Photosynthesis is the process..."
        val expectedDeckId = "deck-12345"

        whenever(deckRepository.generateDeck(testSourceText)).thenReturn(
            Result.success(expectedDeckId)
        )

        viewModel.generateDeck(testSourceText)

        val state = viewModel.uiState.value
        assertTrue("Expected Success state, but was $state", state is GenerateDeckUiState.Success)
        assertEquals(expectedDeckId, (state as GenerateDeckUiState.Success).deckId)
    }

    @Test
    fun `generateDeck on failure sets Error state`() = runBlocking {
        val testSourceText = "Some unparseable AI text..."

        whenever(deckRepository.generateDeck(testSourceText)).thenReturn(
            Result.failure(Exception("AI timeout"))
        )

        viewModel.generateDeck(testSourceText)

        val state = viewModel.uiState.value
        assertTrue("Expected Error state, but was $state", state is GenerateDeckUiState.Error)
        // Since Exception is caught and mapped by toUserFriendlyMessage, it usually provides a safe string.
        // We will just verify it's an Error type and has some message.
        assertTrue((state as GenerateDeckUiState.Error).message.isNotEmpty())
    }
}
