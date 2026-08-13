package com.example.studysyncandroid.util

import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import java.net.ConnectException
import java.net.UnknownHostException
import java.io.IOException

fun Throwable.toUserFriendlyMessage(): String {
    val rawMsg = this.message ?: ""
    if (rawMsg.contains("could not generate", ignoreCase = true) || rawMsg.contains("malformed", ignoreCase = true)) {
        return "Could not generate cards. The notes might be too complex, malformed, or gibberish. Please try with clearer text."
    }
    
    return when (this) {
        is ConnectException, is UnknownHostException -> 
            "Unable to connect to the server. Please check your internet connection."
        is SocketTimeoutException, is ConnectTimeoutException ->
            "The connection timed out. Please try again."
        is ClientRequestException -> {
            when (this.response.status.value) {
                400 -> "Invalid request. Please check your inputs."
                401 -> "Unauthorized. Please log in again."
                403 -> "You do not have permission to perform this action."
                404 -> "The requested resource was not found."
                409 -> "Conflict. This user might already exist."
                429 -> "You are doing that too often. Please try again later."
                else -> "An error occurred (Code: ${this.response.status.value}). Please try again."
            }
        }
        is ServerResponseException -> {
            when (this.response.status.value) {
                500 -> "Our server encountered an internal error. We're working on it."
                502, 503, 504 -> "Our server is currently unavailable. Please try again later."
                else -> "Server error (Code: ${this.response.status.value}). Please try again."
            }
        }
        is IOException -> "A network error occurred. Please check your connection."
        else -> "An unexpected error occurred. Please try again."
    }
}
