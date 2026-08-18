package com.example.studysyncandroid.ui.decks

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import com.example.studysyncandroid.data.local.entities.DeckEntity
import com.example.studysyncandroid.data.local.entities.FolderEntity
import com.example.studysyncandroid.data.local.entities.FolderWithDecks
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class DeckListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var deckListViewModel: DeckListViewModel
    private lateinit var folderViewModel: FolderViewModel
    private lateinit var onDeckClick: (String) -> Unit
    private lateinit var onFolderClick: (String) -> Unit
    private lateinit var onProfileClick: () -> Unit

    private val decksFlow = MutableStateFlow<List<DeckEntity>>(emptyList())
    private val isRefreshingDecksFlow = MutableStateFlow(false)

    private val foldersFlow = MutableStateFlow<List<FolderWithDecks>>(emptyList())
    private val isRefreshingFoldersFlow = MutableStateFlow(false)

    @Before
    fun setup() {
        deckListViewModel = mock()
        folderViewModel = mock()
        onDeckClick = mock()
        onFolderClick = mock()
        onProfileClick = mock()

        whenever(deckListViewModel.decks).thenReturn(decksFlow)
        whenever(deckListViewModel.isRefreshing).thenReturn(isRefreshingDecksFlow)
        whenever(folderViewModel.foldersWithDecks).thenReturn(foldersFlow)
        whenever(folderViewModel.isRefreshing).thenReturn(isRefreshingFoldersFlow)
    }

    @Test
    fun deckListScreen_emptyState_showsEmptyMessage() {
        composeTestRule.setContent {
            DeckListScreen(
                onDeckClick = onDeckClick,
                onFolderClick = onFolderClick,
                onProfileClick = onProfileClick,
                deckListViewModel = deckListViewModel,
                folderViewModel = folderViewModel
            )
        }

        composeTestRule.onNodeWithText("No decks yet").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tap the + button to create\nyour first study deck").assertIsDisplayed()
    }

    @Test
    fun deckListScreen_loadingState_showsProgressIndicator() {
        isRefreshingDecksFlow.value = true

        composeTestRule.setContent {
            DeckListScreen(
                onDeckClick = onDeckClick,
                onFolderClick = onFolderClick,
                onProfileClick = onProfileClick,
                deckListViewModel = deckListViewModel,
                folderViewModel = folderViewModel
            )
        }

        composeTestRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertIsDisplayed()
    }

    @Test
    fun deckListScreen_withContent_showsFoldersAndDecks() {
        val testFolder = FolderWithDecks(
            folder = FolderEntity("f1", "Math Folder", "now"),
            decks = listOf(DeckEntity("d1", "f1", "Math Deck 1", 5, false, "now"))
        )
        val testDeck = DeckEntity("d2", null, "Science Deck", 10, true, "now")

        foldersFlow.value = listOf(testFolder)
        decksFlow.value = listOf(testDeck)

        composeTestRule.setContent {
            DeckListScreen(
                onDeckClick = onDeckClick,
                onFolderClick = onFolderClick,
                onProfileClick = onProfileClick,
                deckListViewModel = deckListViewModel,
                folderViewModel = folderViewModel
            )
        }

        // Check folder
        composeTestRule.onNodeWithText("FOLDERS").assertIsDisplayed()
        composeTestRule.onNodeWithText("Math Folder").assertIsDisplayed()

        // Check uncategorized deck
        composeTestRule.onNodeWithText("DECKS").assertIsDisplayed()
        composeTestRule.onNodeWithText("Science Deck").assertIsDisplayed()
        
        // Click on deck
        composeTestRule.onNodeWithText("Science Deck").performClick()
        verify(onDeckClick).invoke("d2")

        // Click on folder
        composeTestRule.onNodeWithText("Math Folder").performClick()
        verify(onFolderClick).invoke("f1")
    }
}
