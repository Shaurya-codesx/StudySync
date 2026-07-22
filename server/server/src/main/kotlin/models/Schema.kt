package com.example.models

import org.jetbrains.exposed.sql.Table

// 1. Users Table
object Users : Table() {
    val id = integer("id").autoIncrement()
    val username = varchar("username", 50).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)

    override val primaryKey = PrimaryKey(id)
}

// 2. Decks Table
object Decks : Table() {
    val id = integer("id").autoIncrement()
    val title = varchar("title", 100)
    val userId = integer("user_id").references(Users.id)

    override val primaryKey = PrimaryKey(id)
}

// 3. Flashcards Table
object Flashcards : Table() {
    val id = integer("id").autoIncrement()
    val front = text("front")
    val back = text("back")
    val deckId = integer("deck_id").references(Decks.id)

    override val primaryKey = PrimaryKey(id)
}

// 4. Tags Table
object Tags : Table() {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 50).uniqueIndex()

    override val primaryKey = PrimaryKey(id)
}

// 5. DeckTags Table (Many-to-Many Link)
object DeckTags : Table() {
    val deckId = integer("deck_id").references(Decks.id)
    val tagId = integer("tag_id").references(Tags.id)

    override val primaryKey = PrimaryKey(deckId, tagId)
}

// 6. StudyProgress Table (Spaced Repetition)
object StudyProgress : Table() {
    val id = integer("id").autoIncrement()
    val cardId = integer("card_id").references(Flashcards.id)
    val userId = integer("user_id").references(Users.id)

    // Tracks how well the user knows the card (defaults to 2.5 multiplier)
    val easeFactor = float("ease_factor").default(2.5f)
    // How many days until the next review
    val interval = integer("interval").default(0)
    // Unix timestamp for the exact next review time
    val nextReview = long("next_review_timestamp")

    override val primaryKey = PrimaryKey(id)
}