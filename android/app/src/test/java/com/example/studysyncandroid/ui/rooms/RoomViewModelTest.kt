package com.example.studysyncandroid.ui.rooms

import com.example.studysyncandroid.data.local.TokenDataStore
import com.example.studysyncandroid.data.remote.RoomLiveState
import com.example.studysyncandroid.data.remote.WebSocketClient
import com.example.studysyncandroid.data.remote.dto.CreateRoomResponse
import com.example.studysyncandroid.data.remote.dto.GetRoomResponse
import com.example.studysyncandroid.data.remote.dto.JoinRoomResponse
import com.example.studysyncandroid.data.remote.dto.RoomMemberDto
import com.example.studysyncandroid.data.repository.RoomRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import java.util.Base64

@OptIn(ExperimentalCoroutinesApi::class)
class RoomViewModelTest {

    private lateinit var roomRepository: RoomRepository
    private lateinit var webSocketClient: WebSocketClient
    private lateinit var tokenDataStore: TokenDataStore
    
    private lateinit var viewModel: RoomViewModel
    
    private val testDispatcher = UnconfinedTestDispatcher()

    private val fakeRoomState = MutableStateFlow(RoomLiveState())

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        roomRepository = mock()
        webSocketClient = mock()
        tokenDataStore = mock()

        whenever(webSocketClient.roomState).thenReturn(fakeRoomState)

        viewModel = RoomViewModel(roomRepository, webSocketClient, tokenDataStore)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `createRoom with empty name sets error`() = runBlocking {
        viewModel.createRoom("   ")
        
        assertEquals("Room name cannot be empty", viewModel.uiState.value.error)
        verifyNoInteractions(roomRepository)
    }

    @Test
    fun `createRoom success connects to websocket`() = runBlocking {
        val fakeToken = createFakeToken("user123")
        whenever(tokenDataStore.getAccessTokenOnce()).thenReturn(fakeToken)
        whenever(roomRepository.createRoom("Math Room")).thenReturn(
            Result.success(CreateRoomResponse("id-1", "123456", "Math Room"))
        )

        viewModel.createRoom("Math Room")

        assertEquals("123456", viewModel.uiState.value.currentRoomCode)
        assertNull(viewModel.uiState.value.error)
        
        verify(webSocketClient).connect(
            roomCode = eq("123456"),
            roomName = eq("Math Room"),
            hostId = eq(""),
            currentUserId = eq(""),
            initialMembers = eq(emptyList())
        )
    }

    @Test
    fun `joinRoom success fetches details and connects to websocket`() = runBlocking {
        val fakeToken = createFakeToken("user456")
        whenever(tokenDataStore.getAccessTokenOnce()).thenReturn(fakeToken)
        
        whenever(roomRepository.getRoom("654321")).thenReturn(
            Result.success(
                GetRoomResponse("id-2", "Science Room", "user123", true, listOf())
            )
        )
        
        val memberDto = RoomMemberDto("user456", "Alice")
        whenever(roomRepository.joinRoom("654321")).thenReturn(
            Result.success(
                JoinRoomResponse("id-2", "Science Room", listOf(memberDto))
            )
        )

        viewModel.joinRoom("654321")

        assertEquals("654321", viewModel.uiState.value.currentRoomCode)
        assertNull(viewModel.uiState.value.error)
        
        verify(webSocketClient).connect(
            roomCode = eq("654321"),
            roomName = eq("Science Room"),
            hostId = eq("user123"),
            currentUserId = eq(""),
            initialMembers = argThat { size == 1 && get(0).userId == "user456" }
        )
    }
    
    @Test
    fun `leaveRoom disconnects websocket and clears state`() = runBlocking {
        viewModel.leaveRoom()
        
        verify(webSocketClient).disconnect()
        assertNull(viewModel.uiState.value.currentRoomCode)
    }

    // Helper to generate a dummy JWT token with a specific userId
    private fun createFakeToken(userId: String): String {
        val payload = """{"userId":"$userId"}"""
        val encodedPayload = Base64.getUrlEncoder().withoutPadding().encodeToString(payload.toByteArray())
        return "header.$encodedPayload.signature"
    }
}
