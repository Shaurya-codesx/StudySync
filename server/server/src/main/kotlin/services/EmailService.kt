package com.example.services

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Serializable
data class BrevoSender(val name: String, val email: String)

@Serializable
data class BrevoRecipient(val email: String)

@Serializable
data class BrevoEmailRequest(
    val sender: BrevoSender,
    val to: List<BrevoRecipient>,
    val subject: String,
    val textContent: String
)

class EmailService {
    private val apiKey = System.getenv("BREVO_API_KEY")
    private val senderEmail = System.getenv("BREVO_SENDER_EMAIL") ?: "hello@studysync.app"
    private val senderName = "StudySync"

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun sendEmail(toEmail: String, subject: String, body: String) = withContext(Dispatchers.IO) {
        if (apiKey.isNullOrBlank()) {
            println("Email not sent. BREVO_API_KEY missing in .env")
            return@withContext
        }

        try {
            val requestBody = BrevoEmailRequest(
                sender = BrevoSender(name = senderName, email = senderEmail),
                to = listOf(BrevoRecipient(email = toEmail)),
                subject = subject,
                textContent = body
            )

            val response = client.post("https://api.brevo.com/v3/smtp/email") {
                header("api-key", apiKey)
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }

            if (response.status.isSuccess()) {
                println("Email sent successfully to $toEmail via Brevo API")
            } else {
                val errorText = response.bodyAsText()
                println("Failed to send email to $toEmail: ${response.status} - $errorText")
                throw RuntimeException("Failed to dispatch email via Brevo: ${response.status}")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println("Exception while sending email to $toEmail: ${e.message}")
            throw RuntimeException("Failed to dispatch email", e)
        }
    }
}
