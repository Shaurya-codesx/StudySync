package com.example.studysyncandroid.ui.review

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.studysyncandroid.data.local.entities.CardEntity
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID

class ReviewScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun reviewScreen_initialState_showsCardQuestionAndHidesAnswer() {
        val mockViewModel = mock<ReviewViewModel>()
        val dummyCard = CardEntity(
            id = UUID.randomUUID().toString(),
            deckId = UUID.randomUUID().toString(),
            question = "What is the capital of France?",
            answer = "Paris",
            dueDate = "2026-08-16T00:00:00Z"
        )
        
        whenever(mockViewModel.uiState).thenReturn(MutableStateFlow(
            ReviewUiState(
                isLoading = false,
                dueCards = listOf(dummyCard),
                currentCardIndex = 0,
                isAnswerRevealed = false
            )
        ))

        composeTestRule.setContent {
            ReviewScreen(deckId = dummyCard.deckId, viewModel = mockViewModel)
        }

        // Question should be visible
        composeTestRule.onNodeWithText("What is the capital of France?").assertIsDisplayed()
        
        // Answer should NOT be visible
        composeTestRule.onNodeWithText("Paris").assertDoesNotExist()
        
        // Quality buttons should NOT exist yet
        composeTestRule.onNodeWithText("Hard").assertDoesNotExist()
        composeTestRule.onNodeWithText("Good").assertDoesNotExist()
        composeTestRule.onNodeWithText("Perfect").assertDoesNotExist()
        
        // Click the "Reveal Answer" button to reveal answer
        composeTestRule.onNodeWithText("Reveal Answer").performClick()
        
        // Verify reveal action was triggered
        verify(mockViewModel).revealAnswer()
    }

    @Test
    fun reviewScreen_answerRevealed_showsAnswerAndButtons() {
        val mockViewModel = mock<ReviewViewModel>()
        val dummyCard = CardEntity(
            id = UUID.randomUUID().toString(),
            deckId = UUID.randomUUID().toString(),
            question = "What is the capital of France?",
            answer = "Paris",
            dueDate = "2026-08-16T00:00:00Z"
        )
        
        whenever(mockViewModel.uiState).thenReturn(MutableStateFlow(
            ReviewUiState(
                isLoading = false,
                dueCards = listOf(dummyCard),
                currentCardIndex = 0,
                isAnswerRevealed = true // Simulating a flipped card
            )
        ))

        composeTestRule.setContent {
            ReviewScreen(deckId = dummyCard.deckId, viewModel = mockViewModel)
        }

        // Both question and answer should be visible
        composeTestRule.onNodeWithText("What is the capital of France?").assertIsDisplayed()
        composeTestRule.onNodeWithText("Paris").assertIsDisplayed()
        
        // Quality buttons should be visible
        composeTestRule.onNodeWithText("Hard").assertIsDisplayed()
        composeTestRule.onNodeWithText("Good").assertIsDisplayed()
        composeTestRule.onNodeWithText("Perfect").assertIsDisplayed()
        
        // Click the button for "Good" which contains the text "4"
        composeTestRule.onNodeWithText("4").performClick()
        
        // Verify submitRating was called with quality 4 (Good)
        verify(mockViewModel).submitRating(4)
    }
}
