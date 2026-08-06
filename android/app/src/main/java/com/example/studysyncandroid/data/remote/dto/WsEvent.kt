package com.example.studysyncandroid.data.remote.dto

import kotlinx.serialization.Serializable

// Represents both incoming and outgoing WebSocket messages
@Serializable
data class WsEvent(
    val type: String,
    // Fields for user_joined / user_left
    val userId: String? = null,
    val displayName: String? = null,
    // Fields for timer_sync
    val state: String? = null, // "running" or "paused"
    val remainingSeconds: Int? = null,
    // NEW: Field for host_changed
    val newHostId: String? = null,
    // NEW: Field for room_name_changed
    val newName: String? = null,
    // NEW: Field for timer_update (outgoing custom duration)
    val durationSeconds: Int? = null,
    val mode: String? = null,
    val task: String? = null,
    val isTaskDone: Boolean? = null
)