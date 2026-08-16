package com.example.services

import java.util.Properties
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EmailService {
    private val username = System.getenv("SMTP_EMAIL")
    private val password = System.getenv("SMTP_PASSWORD")

    suspend fun sendEmail(toEmail: String, subject: String, body: String) = withContext(Dispatchers.IO) {
        if (username.isNullOrBlank() || password.isNullOrBlank()) {
            println("Email not sent. SMTP credentials missing in .env")
            return@withContext
        }

        val props = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.host", "smtp.gmail.com")
            put("mail.smtp.port", "587")
            put("mail.smtp.ssl.protocols", "TLSv1.2")
            
            // Add strict timeouts (5 seconds) to prevent infinite hanging
            put("mail.smtp.connectiontimeout", "5000")
            put("mail.smtp.timeout", "5000")
            put("mail.smtp.writetimeout", "5000")
        }

        val session = Session.getInstance(props, object : javax.mail.Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(username, password)
            }
        })

        try {
            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(username))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail))
                setSubject(subject)
                setText(body)
            }
            Transport.send(message)
            println("Email sent successfully to $toEmail")
        } catch (e: Exception) {
            e.printStackTrace()
            println("Failed to send email to $toEmail: ${e.message}")
            throw RuntimeException("Failed to dispatch email", e)
        }
    }
}
