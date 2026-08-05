package com.example.studysyncandroid.data.remote

import com.example.studysyncandroid.data.remote.dto.CreateRoomResponse
import com.example.studysyncandroid.data.remote.dto.GetRoomResponse
import com.example.studysyncandroid.data.remote.dto.JoinRoomResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import javax.inject.Inject

class RoomApi @Inject constructor(
    private val client: HttpClient
) {
    suspend fun createRoom(): CreateRoomResponse =
        client.post("/rooms").body()

    suspend fun joinRoom(code: String): JoinRoomResponse =
        client.post("/rooms/$code/join").body()

    suspend fun getRoom(code: String): GetRoomResponse =
        client.get("/rooms/$code").body()
}