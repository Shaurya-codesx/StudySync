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
    val remainingSeconds: Int? = null
)