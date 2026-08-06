package com.example.studysyncandroid.data.remote

import android.util.Log
import com.example.studysyncandroid.data.local.TokenDataStore
import com.example.studysyncandroid.data.remote.dto.WsEvent
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.url
import io.ktor.http.encodedPath
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

// Represents the live state of the room for the UI to observe
data class RoomLiveState(
    val isConnected: Boolean = false,
    val roomName: String = "",       // NEW
    val hostId: String = "",         // NEW
    val currentUserId: String = "",  // NEW: Needed to evaluate isHost
    val members: List<Member> = emptyList(),
    val timerState: String = "paused",
    val remainingSeconds: Int = 0,
    val mode: String = "work"
) {
    data class Member(val userId: String, val displayName: String)

    // Helper property to check if the current user is the host
    val isHost: Boolean get() = hostId.isNotEmpty() && hostId == currentUserId
}

@Singleton
class WebSocketClient @Inject constructor(
    private val client: HttpClient,
    private val tokenDataStore: TokenDataStore
) {
    private var session: WebSocketSession? = null
    private val _roomState = MutableStateFlow(RoomLiveState())
    val roomState: StateFlow<RoomLiveState> = _roomState.asStateFlow()

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun connect(
        roomCode: String,
        roomName: String,
        hostId: String,
        currentUserId: String,
        initialMembers: List<RoomLiveState.Member>
    ) {
        val token = tokenDataStore.getAccessTokenOnce() ?: return

        // Initialize state with room metadata and members fetched from the REST API
        _roomState.value = RoomLiveState(
            isConnected = false,
            roomName = roomName,
            hostId = hostId,
            currentUserId = currentUserId,
            members = initialMembers
        )

        try {
            session = client.webSocketSession {
                url {
                    // 1. Append the path securely
                    encodedPath = "/ws/rooms/$roomCode"

                    // 2. Add the token as a query parameter
                    parameters.append("token", token)

                    // 3. EXPLICITLY set the port to 8080 to prevent Ktor from dropping to port 80
                    port = 8080
                }
            }

            _roomState.update { it.copy(isConnected = true) }
            Log.d("WebSocketClient", "Connected to room: $roomCode")

            // Listen for incoming frames
            while (session?.isActive == true) {
                val frame = session?.incoming?.receive()
                if (frame is Frame.Text) {
                    val text = frame.readText()
                    handleIncomingMessage(text)
                }
            }
        } catch (e: Exception) {
            Log.e("WebSocketClient", "WebSocket disconnected or failed", e)
        } finally {
            _roomState.update { it.copy(isConnected = false) }
        }
    }

    private fun handleIncomingMessage(text: String) {
        try {
            val event = json.decodeFromString<WsEvent>(text)
            when (event.type) {
                "user_joined" -> {
                    if (event.userId != null && event.displayName != null) {
                        _roomState.update { state ->
                            val newMember = RoomLiveState.Member(event.userId, event.displayName)
                            val updatedMembers = if (state.members.none { it.userId == event.userId }) {
                                state.members + newMember
                            } else state.members
                            state.copy(members = updatedMembers)
                        }
                    }
                }
                "user_left" -> {
                    if (event.userId != null) {
                        _roomState.update { state ->
                            state.copy(members = state.members.filterNot { it.userId == event.userId })
                        }
                    }
                }
                "timer_sync" -> {
                    if (event.state != null && event.remainingSeconds != null) {
                        _roomState.update { state ->
                            state.copy(
                                timerState = event.state,
                                remainingSeconds = event.remainingSeconds,
                                mode = event.mode ?: state.mode
                            )
                        }
                    }
                }
                "host_changed" -> {
                    if (event.newHostId != null) {
                        _roomState.update { state ->
                            state.copy(hostId = event.newHostId)
                        }
                    }
                }
                "room_name_changed" -> {
                    if (event.newName != null) {
                        _roomState.update { state ->
                            state.copy(roomName = event.newName)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("WebSocketClient", "Failed to parse WebSocket message: $text", e)
        }
    }

    suspend fun sendTimerStart() {
        val event = WsEvent(type = "timer_start")
        session?.send(Frame.Text(json.encodeToString(event)))
    }

    suspend fun sendTimerPause() {
        val event = WsEvent(type = "timer_pause")
        session?.send(Frame.Text(json.encodeToString(event)))
    }

    suspend fun sendTimerUpdateDuration(seconds: Int, mode: String = "work") {
        val event = WsEvent(type = "timer_update", durationSeconds = seconds, mode = mode)
        session?.send(Frame.Text(json.encodeToString(event)))
    }

    suspend fun disconnect() {
        session?.close()
        session = null
        _roomState.value = RoomLiveState()
    }

    fun decrementTimer() {
        _roomState.update { state ->
            if (state.remainingSeconds > 0) {
                state.copy(remainingSeconds = state.remainingSeconds - 1)
            } else {
                state
            }
        }
    }
}