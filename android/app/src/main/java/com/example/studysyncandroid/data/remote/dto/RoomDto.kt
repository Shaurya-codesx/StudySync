package com.example.studysyncandroid.data.remote.dto

import kotlinx.serialization.Serializable

// NEW: Request to create a room with a name
@Serializable
data class CreateRoomRequest(
    val name: String
)

// NEW: Request to edit a room's name
@Serializable
data class EditRoomNameRequest(
    val name: String
)

@Serializable
data class CreateRoomResponse(
    val roomId: String,
    val code: String,
    val name: String // NEW
)

@Serializable
data class RoomMemberDto(
    val userId: String,
    val displayName: String
)

@Serializable
data class JoinRoomResponse(
    val roomId: String,
    val name: String, // NEW
    val members: List<RoomMemberDto>
)

@Serializable
data class GetRoomResponse(
    val roomId: String,
    val name: String, // NEW
    val hostId: String,
    val isActive: Boolean,
    val members: List<RoomMemberDto>
)