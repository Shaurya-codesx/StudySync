package com.example.services

import com.example.dto.GeneratedCardDto
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.*
import kotlinx.serialization.decodeFromString

import io.ktor.client.plugins.HttpTimeout

class AiService {
    private val apiKey = System.getenv("AI_API_KEY") ?: "mock_test_key"

    private val client = HttpClient(CIO) {
        expectSuccess = true
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 90000
            connectTimeoutMillis = 90000
            socketTimeoutMillis = 90000
        }
    }

    // Configure kotlinx.serialization to be forgiving
    private val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    suspend fun generateFlashcards(sourceText: String): com.example.dto.AiGeneratedDeckResult {
        val maxRetries = 1
        var attempt = 0

        while (attempt <= maxRetries) {
            try {
                // 1. Fetch raw response from LLM
                val rawAiResponse = fetchFromGemini(sourceText)

                // 2. Strip Markdown fences
                val cleanedJson = cleanJsonString(rawAiResponse)

                // 3. Attempt to parse into our strict DTO
                val result = jsonParser.decodeFromString<com.example.dto.AiGeneratedDeckResult>(cleanedJson)

                // 4. Validate business logic shape
                if (result.cards.isEmpty()) {
                    throw Exception("AI returned an empty array of cards.")
                }
                if (result.cards.any { it.question.isBlank() || it.answer.isBlank() }) {
                    throw Exception("AI returned cards with missing questions or answers.")
                }

                return result

            } catch (e: io.ktor.client.plugins.ClientRequestException) {
                println("====== AI SERVICE HTTP ERROR ======")
                println("Status: ${e.response.status}")
                e.printStackTrace()
                println("===================================")
                if (e.response.status == io.ktor.http.HttpStatusCode.TooManyRequests) {
                    throw e // Bubble up rate limits immediately to the user
                }
                
                attempt++
                if (attempt > maxRetries) {
                    throw Exception("Failed to generate valid flashcards after retries: ${e.message}", e)
                }
            } catch (e: Exception) {
                println("====== AI SERVICE CRASH ======")
                println("Attempt $attempt failed.")
                println("Error Type: ${e::class.simpleName}")
                println("Message: ${e.message}")
                e.printStackTrace()
                println("==============================")
                
                attempt++
                if (attempt > maxRetries) {
                    throw Exception("Failed to generate valid flashcards after retries: ${e.message}", e)
                }
            }
        }
        return com.example.dto.AiGeneratedDeckResult("Error", emptyList())
    }

    private suspend fun fetchFromGemini(sourceText: String): String {
        val prompt = """
            You are a highly intelligent study assistant. Your task is to generate a flashcard deck from the provided notes.
            Extract the most important facts, definitions, and concepts.
    
            CRITICAL RULES:
            1. You MUST return ONLY a raw JSON object with exactly two keys: "title" (a concise 3-6 word title summarizing the notes) and "cards" (a JSON array of objects where each object has exactly two keys: "question" and "answer").
            2. Do not include markdown code fences (like ```json), conversational text, or explanations. Just the JSON object.
            3. If the provided text is gibberish, conversational, lacks factual information, or cannot reasonably be turned into flashcards, you MUST return an empty array for cards, like this: { "title": "Error", "cards": [] }
    
            Notes:
            $sourceText
        """.trimIndent()

        val response: HttpResponse = client.post("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent") {
            url { parameters.append("key", apiKey) }
            contentType(ContentType.Application.Json)
            setBody(buildJsonObject {
                put("contents", buildJsonArray {
                    add(buildJsonObject {
                        put("parts", buildJsonArray {
                            add(buildJsonObject {
                                put("text", prompt)
                            })
                        })
                    })
                })
            })
        }

        val responseBody = response.bodyAsText()
        println("==== RAW GEMINI API RESPONSE ====")
        println(responseBody)
        println("=================================")
        
        val jsonResponse = Json.parseToJsonElement(responseBody).jsonObject

        return jsonResponse["candidates"]
            ?.jsonArray?.get(0)
            ?.jsonObject?.get("content")
            ?.jsonObject?.get("parts")
            ?.jsonArray?.get(0)
            ?.jsonObject?.get("text")
            ?.jsonPrimitive?.content
            ?: throw Exception("Failed to extract text from Gemini response.")
    }

    private fun cleanJsonString(raw: String): String {
        val startIndex = raw.indexOf('{')
        val endIndex = raw.lastIndexOf('}')
        if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
            return raw.substring(startIndex, endIndex + 1)
        }
        return raw.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
    }
}