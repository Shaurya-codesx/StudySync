package com.example.studysyncandroid.data.remote

import com.example.studysyncandroid.data.remote.dto.CreateFolderRequest
import com.example.studysyncandroid.data.remote.dto.FolderResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import javax.inject.Inject

class FolderApi @Inject constructor(
    private val client: HttpClient
) {
    suspend fun getFolders(): List<FolderResponse> =
        client.get("/folders").body()

    suspend fun createFolder(name: String): FolderResponse =
        client.post("/folders") {
            contentType(ContentType.Application.Json)
            setBody(CreateFolderRequest(name))
        }.body()

    suspend fun deleteFolder(id: String) {
        client.delete("/folders/$id")
    }
}
