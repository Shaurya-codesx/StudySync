package com.example

import com.example.models.* // Imports your new tables
import io.ktor.server.application.Application
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureDatabase() {
    val dbUrl = System.getenv("DATABASE_URL")
        ?: throw IllegalArgumentException("DATABASE_URL is missing!")

    // Capture the database connection into a variable
    val database = Database.connect(
        url = dbUrl,
        driver = "org.postgresql.Driver",
        user = "user",
        password = "password"
    )

    // Run a transaction to create the tables in PostgreSQL if they don't exist yet
    transaction(database) {
        SchemaUtils.create(Users, Decks, Flashcards, Tags, DeckTags, StudyProgress)
    }

    println("======================Successfully+++++++ connected_____ to&&&&&&&&&& the database and generated tables!!!!!!!!!!!!!")
}