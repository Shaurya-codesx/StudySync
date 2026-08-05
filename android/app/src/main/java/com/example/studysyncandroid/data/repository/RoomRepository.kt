package com.example.studysyncandroid.data.repository

import com.example.studysyncandroid.data.remote.RoomApi
import com.example.studysyncandroid.data.remote.dto.CreateRoomResponse
import com.example.studysyncandroid.data.remote.dto.GetRoomResponse
import com.example.studysyncandroid.data.remote.dto.JoinRoomResponse
import javax.inject.Inject

class RoomRepository @Inject constructor(
    private val roomApi: RoomApi
) {
    suspend fun createRoom(): Result<CreateRoomResponse> = runCatching {
        roomApi.createRoom()
    }

    suspend fun joinRoom(code: String): Result<JoinRoomResponse> = runCatching {
        roomApi.joinRoom(code)
    }

    suspend fun getRoom(code: String): Result<GetRoomResponse> = runCatching {
        roomApi.getRoom(code)
    }
}