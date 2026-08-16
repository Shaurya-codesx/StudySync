package com.example.studysyncandroid.ui.decks

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class GenerateDeckScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun generateDeckScreen_initialState_showsInputAndDisabledButton() {
        val mockViewModel = mock<GenerateDeckViewModel>()
        whenever(mockViewModel.uiState).thenReturn(MutableStateFlow(GenerateDeckUiState.Idle))

        composeTestRule.setContent {
            GenerateDeckScreen(
                onDeckGenerated = {},
                onBackClick = {},
                viewModel = mockViewModel
            )
        }

        // Verify elements exist
        composeTestRule.onNodeWithText("Paste Your Notes").assertIsDisplayed()
        
        // The text field placeholder should exist
        composeTestRule.onNodeWithText("Start typing or paste your study notes here...").assertIsDisplayed()
        
        // The generate button should exist but be disabled (because input is empty)
        composeTestRule.onNodeWithText("Generate Deck").assertIsNotEnabled()
    }

    @Test
    fun generateDeckScreen_enteringNotes_enablesButtonAndTriggersViewModel() {
        val mockViewModel = mock<GenerateDeckViewModel>()
        whenever(mockViewModel.uiState).thenReturn(MutableStateFlow(GenerateDeckUiState.Idle))

        composeTestRule.setContent {
            GenerateDeckScreen(
                onDeckGenerated = {},
                onBackClick = {},
                viewModel = mockViewModel
            )
        }

        // Type notes
        val notes = "Mitochondria is the powerhouse of the cell."
        composeTestRule.onNodeWithText("Start typing or paste your study notes here...")
            .performTextInput(notes)

        // Button should be enabled now
        composeTestRule.onNodeWithText("Generate Deck").assertIsEnabled()

        // Click generate
        composeTestRule.onNodeWithText("Generate Deck").performClick()

        // Verify the ViewModel function was called
        verify(mockViewModel).generateDeck(notes)
    }

    @Test
    fun generateDeckScreen_loadingState_showsGeneratingMagicText() {
        val mockViewModel = mock<GenerateDeckViewModel>()
        whenever(mockViewModel.uiState).thenReturn(MutableStateFlow(GenerateDeckUiState.Loading))

        composeTestRule.setContent {
            GenerateDeckScreen(
                onDeckGenerated = {},
                onBackClick = {},
                viewModel = mockViewModel
            )
        }

        // Loading text should be visible instead of standard "Generate Deck" text
        composeTestRule.onNodeWithText("Generating magic...").assertIsDisplayed()
        composeTestRule.onNodeWithText("Generate Deck").assertDoesNotExist()
    }

    @Test
    fun generateDeckScreen_errorState_showsErrorMessage() {
        val mockViewModel = mock<GenerateDeckViewModel>()
        whenever(mockViewModel.uiState).thenReturn(MutableStateFlow(GenerateDeckUiState.Error("Notes are too short.")))

        composeTestRule.setContent {
            GenerateDeckScreen(
                onDeckGenerated = {},
                onBackClick = {},
                viewModel = mockViewModel
            )
        }

        // Error message should be visible
        composeTestRule.onNodeWithText("Notes are too short.").assertIsDisplayed()
    }
}
