package com.example.studysyncandroid.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class FolderResponse(
    val id: String,
    val name: String,
    val createdAt: String
)

@Serializable
data class CreateFolderRequest(
    val name: String
)
