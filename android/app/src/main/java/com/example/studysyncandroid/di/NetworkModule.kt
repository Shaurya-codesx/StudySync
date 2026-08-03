package com.example.studysyncandroid.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.url
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // Emulator loopback to your machine's localhost:8080 (docker-compose server).
    // Swap this for your deployed Railway/Fly.io URL when testing against prod,
    // or move it to a BuildConfig field per build type later.
    private const val BASE_URL = "http://10.0.2.2:8080"

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                }
            )
        }
        install(Logging) {
            level = LogLevel.ALL
        }
        install(WebSockets)

        defaultRequest {
            url(BASE_URL)
            // TODO (Phase 11): attach "Authorization: Bearer <accessToken>"
            // here once the DataStore-backed token repository exists, and
            // wire up 401 -> /auth/refresh -> retry logic.
        }
    }
}