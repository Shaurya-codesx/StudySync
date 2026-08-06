package com.example.studysyncandroid.ui.rooms

import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studysyncandroid.data.local.TokenDataStore
import com.example.studysyncandroid.data.remote.RoomLiveState
import com.example.studysyncandroid.data.remote.WebSocketClient
import com.example.studysyncandroid.data.repository.RoomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

data class RoomUiState(
    val isLoading: Boolean = false,
    val currentRoomCode: String? = null,
    val error: String? = null
)

@HiltViewModel
class RoomViewModel @Inject constructor(
    private val roomRepository: RoomRepository,
    private val webSocketClient: WebSocketClient,
    private val tokenDataStore: TokenDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoomUiState())
    val uiState: StateFlow<RoomUiState> = _uiState.asStateFlow()

    val liveState: StateFlow<RoomLiveState> = webSocketClient.roomState

    init {
        // Local Ticker: Counts down every second while the timer state is "running"
        viewModelScope.launch {
            while (true) {
                delay(1000)
                if (liveState.value.timerState == "running") {
                    webSocketClient.decrementTimer()
                }
            }
        }
    }

    fun createRoom(roomName: String) {
        if (roomName.isBlank()) {
            _uiState.update { it.copy(error = "Room name cannot be empty") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val token = tokenDataStore.getAccessTokenOnce()
            val myUserId = if (token != null) extractUserIdFromToken(token) else ""

            roomRepository.createRoom(roomName).fold(
                onSuccess = { response ->
                    _uiState.update { it.copy(isLoading = false, currentRoomCode = response.code) }
                    webSocketClient.connect(
                        roomCode = response.code,
                        roomName = response.name,
                        hostId = myUserId,
                        currentUserId = myUserId,
                        initialMembers = emptyList()
                    )
                },
                onFailure = { err ->
                    _uiState.update { it.copy(isLoading = false, error = err.message ?: "Failed to create room") }
                }
            )
        }
    }

    fun joinRoom(code: String) {
        if (code.isBlank()) {
            _uiState.update { it.copy(error = "Room code cannot be empty") }
            return
        }

        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val token = tokenDataStore.getAccessTokenOnce()
            val myUserId = if (token != null) extractUserIdFromToken(token) else ""

            roomRepository.getRoom(code).fold(
                onSuccess = { detailsResponse ->
                    roomRepository.joinRoom(code).fold(
                        onSuccess = { joinResponse ->
                            _uiState.update { it.copy(isLoading = false, currentRoomCode = code) }
                            val initialMembers = joinResponse.members.map {
                                RoomLiveState.Member(it.userId, it.displayName)
                            }
                            webSocketClient.connect(
                                roomCode = code,
                                roomName = detailsResponse.name,
                                hostId = detailsResponse.hostId,
                                currentUserId = myUserId,
                                initialMembers = initialMembers
                            )
                        },
                        onFailure = { err ->
                            _uiState.update { it.copy(isLoading = false, error = err.message ?: "Failed to join room") }
                        }
                    )
                },
                onFailure = { err ->
                    _uiState.update { it.copy(isLoading = false, error = err.message ?: "Room not found") }
                }
            )
        }
    }

    fun startTimer() {
        viewModelScope.launch {
            webSocketClient.sendTimerStart()
        }
    }

    fun pauseTimer() {
        viewModelScope.launch {
            webSocketClient.sendTimerPause()
        }
    }

    fun changeTimerDuration(minutes: Int, mode: String = "work") {
        viewModelScope.launch {
            webSocketClient.sendTimerUpdateDuration(minutes * 60, mode)
        }
    }

    fun renameRoom(newName: String) {
        val code = uiState.value.currentRoomCode ?: return
        if (newName.isBlank()) return

        viewModelScope.launch {
            roomRepository.editRoomName(code, newName)
        }
    }

    fun leaveRoom() {
        viewModelScope.launch {
            webSocketClient.disconnect()
            _uiState.update { RoomUiState() }
        }
    }

    private fun extractUserIdFromToken(token: String): String {
        return try {
            val parts = token.split(".")
            if (parts.size == 3) {
                val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
                JSONObject(payload).optString("userId", "")
            } else ""
        } catch (e: Exception) {
            ""
        }
    }

    // Add this inside RoomViewModel, right at the bottom
    override fun onCleared() {
        super.onCleared()
        // Guarantee we disconnect from the WebSocket if the ViewModel is destroyed
        viewModelScope.launch {
            webSocketClient.disconnect()
        }
    }
}