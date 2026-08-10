package com.example.studysyncandroid.di

import com.example.studysyncandroid.data.local.TokenDataStore
import com.example.studysyncandroid.data.remote.dto.AuthResponse
import com.example.studysyncandroid.data.remote.dto.RefreshRequest
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.encodedPath
import com.example.studysyncandroid.data.remote.FolderApi
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // Emulator loopback to your machine's localhost:8080 (docker-compose server).
    // Swap this for your deployed Railway/Fly.io URL when testing against prod.
    private const val BASE_URL = "http://10.0.2.2:8080"

    @Provides
    @Singleton
    fun provideHttpClient(tokenDataStore: TokenDataStore): HttpClient = HttpClient(OkHttp) {
        expectSuccess = true // non-2xx responses throw, so repositories can runCatching { }

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

        install(Auth) {
            bearer {
                loadTokens {
                    val access = tokenDataStore.getAccessTokenOnce()
                    val refresh = tokenDataStore.getRefreshTokenOnce()
                    if (access != null && refresh != null) {
                        BearerTokens(access, refresh)
                    } else {
                        null
                    }
                }

                refreshTokens {
                    val currentRefreshToken = oldTokens?.refreshToken
                    if (currentRefreshToken == null) {
                        null
                    } else {
                        try {
                            val response: AuthResponse = client.post("/auth/refresh") {
                                markAsRefreshTokenRequest()
                                contentType(ContentType.Application.Json)
                                setBody(RefreshRequest(currentRefreshToken))
                            }.body()
                            tokenDataStore.saveTokens(response.accessToken, response.refreshToken)
                            BearerTokens(response.accessToken, response.refreshToken)
                        } catch (e: Exception) {
                            tokenDataStore.clearTokens()
                            null
                        }
                    }
                }

                // Don't attach or trigger-refresh-on-401 for the auth endpoints themselves —
                // login/signup have no tokens yet, and a 401 there means "wrong password,"
                // not "expired session."
                sendWithoutRequest { request ->
                    !request.url.encodedPath.contains("/auth/")
                }
            }
        }

        defaultRequest {
            url(BASE_URL)
        }
    }

    @Provides
    @Singleton
    fun provideFolderApi(client: HttpClient): FolderApi = FolderApi(client)

    @Provides
    @Singleton
    fun provideMarketplaceApi(client: HttpClient): com.example.studysyncandroid.data.remote.MarketplaceApi = com.example.studysyncandroid.data.remote.MarketplaceApi(client)

    @Provides
    @Singleton
    fun provideAnalyticsApi(client: HttpClient): com.example.studysyncandroid.data.remote.AnalyticsApi = com.example.studysyncandroid.data.remote.AnalyticsApi(client)
}