package com.example.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateRoomRequest(val name: String)

@Serializable
data class EditRoomNameRequest(val name: String)


@Serializable
data class CreateRoomResponse(
    val roomId: String,
    val code: String,
    val name: String
)

@Serializable
data class RoomMemberDto(
    val userId: String,
    val displayName: String
)

@Serializable
data class JoinRoomResponse(
    val roomId: String,
    val name: String,
    val members: List<RoomMemberDto>
)

@Serializable
data class RoomDetailsResponse(
    val roomId: String,
    val name: String,
    val hostId: String,
    val isActive: Boolean,
    val members: List<RoomMemberDto>
)

// Used to parse incoming WebSocket client messages: {"type": "timer_start"} etc.
@Serializable
data class IncomingRoomEvent(
    val type: String,
    val durationSeconds: Int? = null
)