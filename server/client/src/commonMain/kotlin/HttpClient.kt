package com.example

import io.ktor.client.HttpClient
import io.ktor.client.*
import kotlinx.rpc.krpc.ktor.client.installKrpc

val httpClient = HttpClient {
    installKrpc()
}
