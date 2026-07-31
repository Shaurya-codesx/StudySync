package com.example.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateRoomResponse(
    val roomId: String,
    val code: String
)

@Serializable
data class RoomMemberDto(
    val userId: String,
    val displayName: String
)

@Serializable
data class JoinRoomResponse(
    val roomId: String,
    val members: List<RoomMemberDto>
)

@Serializable
data class RoomDetailsResponse(
    val roomId: String,
    val hostId: String,
    val isActive: Boolean,
    val members: List<RoomMemberDto>
)

// Used to parse incoming WebSocket client messages: {"type": "timer_start"} etc.
@Serializable
data class IncomingRoomEvent(
    val type: String
)