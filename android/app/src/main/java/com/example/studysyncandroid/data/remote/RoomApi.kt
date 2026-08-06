package com.example.studysyncandroid.data.remote

import com.example.studysyncandroid.data.remote.dto.CreateRoomRequest
import com.example.studysyncandroid.data.remote.dto.CreateRoomResponse
import com.example.studysyncandroid.data.remote.dto.EditRoomNameRequest
import com.example.studysyncandroid.data.remote.dto.GetRoomResponse
import com.example.studysyncandroid.data.remote.dto.JoinRoomResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import javax.inject.Inject

class RoomApi @Inject constructor(
    private val client: HttpClient
) {
    suspend fun createRoom(name: String): CreateRoomResponse =
        client.post("/rooms") {
            contentType(ContentType.Application.Json)
            setBody(CreateRoomRequest(name = name))
        }.body()

    suspend fun joinRoom(code: String): JoinRoomResponse =
        client.post("/rooms/$code/join").body()

    suspend fun getRoom(code: String): GetRoomResponse =
        client.get("/rooms/$code").body()

    // NEW: Endpoint to rename the room
    suspend fun editRoomName(code: String, newName: String) {
        client.patch("/rooms/$code/name") {
            contentType(ContentType.Application.Json)
            setBody(EditRoomNameRequest(name = newName))
        }
    }
}