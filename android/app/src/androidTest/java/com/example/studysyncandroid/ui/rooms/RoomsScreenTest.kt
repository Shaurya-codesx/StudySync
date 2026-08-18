package com.example.studysyncandroid.ui.rooms

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.example.studysyncandroid.data.remote.RoomLiveState
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class RoomsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var roomViewModel: RoomViewModel

    private val uiStateFlow = MutableStateFlow(RoomUiState())
    private val liveStateFlow = MutableStateFlow(RoomLiveState())

    @Before
    fun setup() {
        roomViewModel = mock()
        whenever(roomViewModel.uiState).thenReturn(uiStateFlow)
        whenever(roomViewModel.liveState).thenReturn(liveStateFlow)
    }

    @Test
    fun roomsScreen_inLobby_showsCreateAndJoin() {
        composeTestRule.setContent {
            RoomsScreen(viewModel = roomViewModel)
        }

        composeTestRule.onNodeWithText("Study Rooms").assertIsDisplayed()
        composeTestRule.onNodeWithText("New Room Name").assertIsDisplayed()
        composeTestRule.onNodeWithText("Create New Room").assertIsDisplayed()
        composeTestRule.onNodeWithText("Enter 6-Digit Code").assertIsDisplayed()
        composeTestRule.onNodeWithText("Join Room").assertIsDisplayed()
    }

    @Test
    fun roomsScreen_inRoom_showsRoomContentAndHostControls() {
        uiStateFlow.value = RoomUiState(currentRoomCode = "123456")
        liveStateFlow.value = RoomLiveState(
            isConnected = true,
            roomName = "Focus Room",
            hostId = "user1",
            currentUserId = "user1",
            members = listOf(
                RoomLiveState.Member("user1", "Alice", "Math", false)
            ),
            timerState = "paused",
            remainingSeconds = 1500, // 25:00
            mode = "work"
        )

        composeTestRule.setContent {
            RoomsScreen(viewModel = roomViewModel)
        }

        composeTestRule.onNodeWithText("Focus Room").assertIsDisplayed()
        composeTestRule.onNodeWithText("Code · 123456").assertIsDisplayed()
        composeTestRule.onNodeWithText("25:00").assertIsDisplayed()
        
        // Host controls
        composeTestRule.onNodeWithText("Start timer").assertIsDisplayed()
        
        // Members list
        composeTestRule.onNodeWithText("Alice (You)").assertIsDisplayed()
    }

    @Test
    fun roomsScreen_inRoomAsGuest_hidesHostControls() {
        uiStateFlow.value = RoomUiState(currentRoomCode = "123456")
        liveStateFlow.value = RoomLiveState(
            isConnected = true,
            roomName = "Focus Room",
            hostId = "user1",
            currentUserId = "user2", // Not host
            members = listOf(
                RoomLiveState.Member("user1", "Alice", "Math", false),
                RoomLiveState.Member("user2", "Bob", "Science", false)
            ),
            timerState = "paused",
            remainingSeconds = 1500,
            mode = "work"
        )

        composeTestRule.setContent {
            RoomsScreen(viewModel = roomViewModel)
        }

        // Guest message instead of host controls
        composeTestRule.onNodeWithText("The host controls the timer.").assertIsDisplayed()
        composeTestRule.onNodeWithText("Start timer").assertDoesNotExist()
        
        // Members list
        composeTestRule.onNodeWithText("Bob (You)").assertIsDisplayed()
    }
}
