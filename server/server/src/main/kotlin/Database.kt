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

    // 1. Parse the standard Postgres URL string
    val uri = URI(if (rawDbUrl.startsWith("postgres")) rawDbUrl else "postgresql://$rawDbUrl")

    // 2. Extract the username and password provided by Railway
    val userInfo = uri.userInfo?.split(":")
    val parsedUser = userInfo?.getOrNull(0) ?: ""
    val parsedPassword = userInfo?.getOrNull(1) ?: ""

    // 3. Construct the JDBC-specific URL format that Exposed requires
    val jdbcUrl = "jdbc:postgresql://${uri.host}:${uri.port}${uri.path}"

    // 4. Capture the database connection into a variable using the parsed credentials
    val database = Database.connect(
        url = jdbcUrl,
        driver = "org.postgresql.Driver",
        user = parsedUser,
        password = parsedPassword
    )

    // Run a transaction to create the tables in PostgreSQL if they don't exist yet
    transaction(database) {
        SchemaUtils.create(Users, Otps, Folders, Decks, Cards, ReviewLogs, StudyRooms, RoomMembers)
    }

    println("======================Successfully+++++++ connected_____ to&&&&&&&&&& the database and generated tables!!!!!!!!!!!!!")
}