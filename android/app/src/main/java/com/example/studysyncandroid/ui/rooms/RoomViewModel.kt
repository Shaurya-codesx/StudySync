package com.example.studysyncandroid.ui.rooms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studysyncandroid.data.remote.RoomLiveState
import com.example.studysyncandroid.data.remote.WebSocketClient
import com.example.studysyncandroid.data.repository.RoomRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RoomUiState(
    val isLoading: Boolean = false,
    val currentRoomCode: String? = null,
    val error: String? = null
)

@HiltViewModel
class RoomViewModel @Inject constructor(
    private val roomRepository: RoomRepository,
    private val webSocketClient: WebSocketClient
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoomUiState())
    val uiState: StateFlow<RoomUiState> = _uiState.asStateFlow()

    // Expose the live WebSocket state directly to the UI
    val liveState: StateFlow<RoomLiveState> = webSocketClient.roomState

    init {
        // Local Ticker: Counts down every second while the timer is running
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(1000)
                if (liveState.value.timerState == "running") {
                    webSocketClient.decrementTimer()
                }
            }
        }
    }

    fun createRoom() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            roomRepository.createRoom().fold(
                onSuccess = { response ->
                    _uiState.update { it.copy(isLoading = false, currentRoomCode = response.code) }
                    // Host has no initial members besides themselves, which the socket will broadcast
                    connectToSocket(response.code, emptyList())
                },
                onFailure = {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to create room.") }
                }
            )
        }
    }

    fun joinRoom(code: String) {
        if (code.isBlank()) return

        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            roomRepository.joinRoom(code).fold(
                onSuccess = { response ->
                    _uiState.update { it.copy(isLoading = false, currentRoomCode = code) }

                    // Map REST members to WebSocket LiveState members so UI doesn't blink empty
                    val initialMembers = response.members.map {
                        RoomLiveState.Member(it.userId, it.displayName)
                    }
                    connectToSocket(code, initialMembers)
                },
                onFailure = {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to join. Invalid code?") }
                }
            )
        }
    }

    private fun connectToSocket(code: String, initialMembers: List<RoomLiveState.Member>) {
        viewModelScope.launch {
            webSocketClient.connect(code, initialMembers)
        }
    }

    fun toggleTimer() {
        viewModelScope.launch {
            if (liveState.value.timerState == "running") {
                webSocketClient.sendTimerPause()
            } else {
                webSocketClient.sendTimerStart()
            }
        }
    }

    fun leaveRoom() {
        viewModelScope.launch {
            webSocketClient.disconnect()
            _uiState.update { it.copy(currentRoomCode = null) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Ensure we drop the socket connection if the ViewModel is destroyed
        viewModelScope.launch {
            webSocketClient.disconnect()
        }
    }
}