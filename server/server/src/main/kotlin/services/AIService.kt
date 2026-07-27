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

class AiService {
    private val apiKey = System.getenv("AI_API_KEY")
        ?: throw IllegalArgumentException("AI_API_KEY environment variable is missing")

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    // Configure kotlinx.serialization to be forgiving
    private val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    suspend fun generateFlashcards(sourceText: String): List<GeneratedCardDto> {
        val maxRetries = 1
        var attempt = 0

        while (attempt <= maxRetries) {
            try {
                // 1. Fetch raw response from LLM
                val rawAiResponse = fetchFromGemini(sourceText)

                // 2. Strip Markdown fences
                val cleanedJson = cleanJsonString(rawAiResponse)

                // 3. Attempt to parse into our strict DTO
                val cards = jsonParser.decodeFromString<List<GeneratedCardDto>>(cleanedJson)

                // 4. Validate business logic shape
                if (cards.isEmpty()) {
                    throw Exception("AI returned an empty array of cards.")
                }
                if (cards.any { it.question.isBlank() || it.answer.isBlank() }) {
                    throw Exception("AI returned cards with missing questions or answers.")
                }

                return cards

            } catch (e: Exception) {
                attempt++
                if (attempt > maxRetries) {
                    // This exception will be caught by StatusPages and mapped to a 502 Bad Gateway
                    throw Exception("Failed to generate valid flashcards after retries: ${e.message}")
                }
                // If it failed but we have retries left, the loop will just run again
            }
        }
        return emptyList()
    }

    private suspend fun fetchFromGemini(sourceText: String): String {
        val prompt = """
            You are a highly intelligent study assistant. Your task is to generate a flashcard deck from the provided notes.
            Extract the most important facts, definitions, and concepts.
    
            CRITICAL RULES:
            1. You MUST return ONLY a raw JSON array of objects, where each object has exactly two keys: "question" and "answer".
            2. Do not include markdown code fences (like ```json), conversational text, or explanations. Just the JSON array.
            3. If the provided text is gibberish, conversational, lacks factual information, or cannot reasonably be turned into flashcards, you MUST return an empty array: []
    
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
        return raw.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
    }
}