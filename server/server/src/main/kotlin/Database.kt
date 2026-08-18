package com.example

import com.example.models.* // Imports your new tables
import io.ktor.server.application.Application
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.net.URI

fun Application.configureDatabase() {
    val rawDbUrl = System.getenv("DATABASE_URL")
        ?: throw IllegalArgumentException("DATABASE_URL is missing!")

    val jdbcUrl: String
    val parsedUser: String
    val parsedPassword: String

    if (rawDbUrl.startsWith("jdbc:")) {
        // Already a JDBC URL (used in local tests or CI)
        jdbcUrl = rawDbUrl
        parsedUser = System.getenv("POSTGRES_USER") ?: "user"
        parsedPassword = System.getenv("POSTGRES_PASSWORD") ?: "password"
    } else {
        // Standard Postgres URL (used in Railway)
        val uri = URI(if (rawDbUrl.startsWith("postgres")) rawDbUrl else "postgresql://$rawDbUrl")
        val userInfo = uri.userInfo?.split(":")
        parsedUser = userInfo?.getOrNull(0) ?: ""
        parsedPassword = userInfo?.getOrNull(1) ?: ""
        jdbcUrl = "jdbc:postgresql://${uri.host}:${uri.port}${uri.path}"
    }

    val driverClass = if (jdbcUrl.startsWith("jdbc:h2")) {
        "org.h2.Driver"
    } else {
        "org.postgresql.Driver"
    }

    // Capture the database connection into a variable using the parsed credentials
    val database = Database.connect(
        url = jdbcUrl,
        driver = driverClass,
        user = parsedUser,
        password = parsedPassword
    )

    // Run a transaction to create the tables in PostgreSQL if they don't exist yet
    transaction(database) {
        SchemaUtils.create(Users, Otps, Folders, Decks, Cards, ReviewLogs, StudyRooms, RoomMembers)
    }

    println("======================Successfully+++++++ connected_____ to&&&&&&&&&& the database and generated tables!!!!!!!!!!!!!")
}