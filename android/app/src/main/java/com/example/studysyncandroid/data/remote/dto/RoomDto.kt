package com.example.studysyncandroid.data.remote.dto

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
data class GetRoomResponse(
    val roomId: String,
    val hostId: String,
    val isActive: Boolean,
    val members: List<RoomMemberDto>
)