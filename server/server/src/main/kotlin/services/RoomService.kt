package com.example.services

import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Serializable
data class UserJoinedEvent(val type: String = "user_joined", val userId: String, val displayName: String)

@Serializable
data class UserLeftEvent(val type: String = "user_left", val userId: String)

@Serializable
data class TimerSyncEvent(val type: String = "timer_sync", val state: String, val remainingSeconds: Int, val mode: String = "work")

@Serializable
data class HostChangedEvent(val type: String = "host_changed", val newHostId: String)

// NEW: Event to broadcast when the host changes the room name
@Serializable
data class RoomNameChangedEvent(val type: String = "room_name_changed", val newName: String)

@Serializable
data class TaskUpdatedEvent(
    val type: String = "task_update",
    val userId: String,
    val task: String,
    val isTaskDone: Boolean
)

private data class ConnectedMember(
    val userId: UUID,
    val displayName: String,
    val session: DefaultWebSocketServerSession,
    var task: String = "",
    var isTaskDone: Boolean = false
)

private class RoomState(var hostId: UUID) {
    val members = ConcurrentHashMap<UUID, ConnectedMember>()
    var isTimerRunning: Boolean = false
    var remainingSeconds: Int = DEFAULT_TIMER_SECONDS
    var timerStartedAtEpochMillis: Long? = null
    var mode: String = "work"
    val mutex = Mutex()

    companion object {
        const val DEFAULT_TIMER_SECONDS = 25 * 60
    }
}

data class LeaveResult(val isRoomEmpty: Boolean, val newHostId: UUID? = null)

class RoomService {

    private val rooms = ConcurrentHashMap<String, RoomState>()
    private val json = Json { encodeDefaults = true }

    suspend fun join(
        code: String,
        hostId: UUID,
        userId: UUID,
        displayName: String,
        session: DefaultWebSocketServerSession
    ) {
        val room = rooms.getOrPut(code) { RoomState(hostId) }
        room.members[userId] = ConnectedMember(userId, displayName, session)

        broadcast(room, json.encodeToString(UserJoinedEvent.serializer(), UserJoinedEvent(userId = userId.toString(), displayName = displayName)))

        // sync the newly joined client with current timer state immediately,
        // rather than making them wait for the next start/pause event
        sendCurrentTimerState(room, session)
    }

    suspend fun leave(code: String, userId: UUID): LeaveResult {
        val room = rooms[code] ?: return LeaveResult(isRoomEmpty = true)

        room.members.remove(userId)
        broadcast(room, json.encodeToString(UserLeftEvent.serializer(), UserLeftEvent(userId = userId.toString())))

        if (room.members.isEmpty()) {
            rooms.remove(code) // avoid leaking memory for abandoned rooms
            return LeaveResult(isRoomEmpty = true) // Tell the router the room is empty
        }

        if (userId == room.hostId) {
            val newHostId = room.members.keys.first()
            room.hostId = newHostId
            broadcast(room, json.encodeToString(HostChangedEvent.serializer(), HostChangedEvent(newHostId = newHostId.toString())))
            return LeaveResult(isRoomEmpty = false, newHostId = newHostId)
        }

        return LeaveResult(isRoomEmpty = false)
    }

    suspend fun handleTimerStart(code: String, requestingUserId: UUID) {
        val room = rooms[code] ?: return
        if (requestingUserId != room.hostId) return // host-controlled only

        room.mutex.withLock {
            if (!room.isTimerRunning) {
                room.isTimerRunning = true
                room.timerStartedAtEpochMillis = System.currentTimeMillis()
            }
        }
        broadcastTimerSync(room)
    }

    suspend fun handleTimerPause(code: String, requestingUserId: UUID) {
        val room = rooms[code] ?: return
        if (requestingUserId != room.hostId) return

        room.mutex.withLock {
            if (room.isTimerRunning) {
                room.remainingSeconds = currentRemaining(room)
                room.isTimerRunning = false
                room.timerStartedAtEpochMillis = null
            }
        }
        broadcastTimerSync(room)
    }

    // NEW: Broadcasts the updated name to all clients instantly
    suspend fun broadcastNameChange(code: String, newName: String) {
        val room = rooms[code] ?: return
        val event = RoomNameChangedEvent(newName = newName)
        broadcast(room, json.encodeToString(RoomNameChangedEvent.serializer(), event))
    }

    // NEW: Allows the host to change the timer duration (only when paused)
    suspend fun handleTimerUpdateDuration(code: String, requestingUserId: UUID, newDurationSeconds: Int, newMode: String?) {
        val room = rooms[code] ?: return
        if (requestingUserId != room.hostId) return // Only host can change time

        room.mutex.withLock {
            if (!room.isTimerRunning) {
                room.remainingSeconds = newDurationSeconds
                if (newMode != null) {
                    room.mode = newMode // Update the mode if provided
                }
            }
        }
        broadcastTimerSync(room)
    }

    // NEW: Handle a user updating their task or checking it off
    suspend fun handleTaskUpdate(code: String, requestingUserId: UUID, task: String, isTaskDone: Boolean) {
        val room = rooms[code] ?: return

        // 1. Find the user and update their state in memory
        val member = room.members[requestingUserId] ?: return
        member.task = task
        member.isTaskDone = isTaskDone

        // 2. Broadcast the update to everyone in the room
        val event = TaskUpdatedEvent(
            userId = requestingUserId.toString(),
            task = task,
            isTaskDone = isTaskDone
        )
        broadcast(room, json.encodeToString(TaskUpdatedEvent.serializer(), event))
    }

    private fun currentRemaining(room: RoomState): Int {
        if (!room.isTimerRunning) return room.remainingSeconds
        val startedAt = room.timerStartedAtEpochMillis ?: return room.remainingSeconds
        val elapsedSeconds = ((System.currentTimeMillis() - startedAt) / 1000).toInt()
        return (room.remainingSeconds - elapsedSeconds).coerceAtLeast(0)
    }

    private suspend fun broadcastTimerSync(room: RoomState) {
        val event = TimerSyncEvent(
            state = if (room.isTimerRunning) "running" else "paused",
            remainingSeconds = currentRemaining(room),
            mode = room.mode // NEW
        )
        broadcast(room, json.encodeToString(TimerSyncEvent.serializer(), event))
    }

    private suspend fun sendCurrentTimerState(room: RoomState, session: DefaultWebSocketServerSession) {
        val event = TimerSyncEvent(
            state = if (room.isTimerRunning) "running" else "paused",
            remainingSeconds = currentRemaining(room),
            mode = room.mode // NEW
        )
        try {
            session.send(Frame.Text(json.encodeToString(TimerSyncEvent.serializer(), event)))
        } catch (e: Exception) {
            // session may have already closed; ignore
        }
    }

    private suspend fun broadcast(room: RoomState, text: String) {
        room.members.values.forEach { member ->
            try {
                member.session.send(Frame.Text(text))
            } catch (e: Exception) {
                // dead session — will be cleaned up when its own disconnect handler runs
            }
        }
    }
}