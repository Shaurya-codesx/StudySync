package com.example.models

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.CurrentDateTime
import org.jetbrains.exposed.sql.javatime.datetime

// 1. Users Table
object Users : Table("users") {
    val id = uuid("id").autoGenerate()
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = text("password_hash")
    val displayName = varchar("display_name", 100).nullable()
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(id)
}

// 2. Decks Table
object Decks : Table("decks") {
    val id = uuid("id").autoGenerate()
    val userId = uuid("user_id").references(Users.id)
    val title = text("title")
    val sourceText = text("source_text").nullable()
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(id)
}

// 3. Cards Table
object Cards : Table("cards") {
    val id = uuid("id").autoGenerate()
    val deckId = uuid("deck_id").references(Decks.id)
    val question = text("question")
    val answer = text("answer")
    val easeFactor = float("ease_factor").default(2.5f)
    val intervalDays = integer("interval_days").default(0)
    val repetitions = integer("repetitions").default(0)
    val dueDate = datetime("due_date").defaultExpression(CurrentDateTime)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(id)
}

// 4. Review Logs Table
object ReviewLogs : Table("review_logs") {
    val id = uuid("id").autoGenerate()
    val cardId = uuid("card_id").references(Cards.id)
    val quality = integer("quality")
    val reviewedAt = datetime("reviewed_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(id)
}

// 5. Study Rooms Table
object StudyRooms : Table("study_rooms") {
    val id = uuid("id").autoGenerate()
    val code = varchar("code", 6).uniqueIndex()
    val name = varchar("name", 100)
    val hostId = uuid("host_id").references(Users.id)
    val isActive = bool("is_active").default(true)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(id)
}

// 6. Room Members Table
object RoomMembers : Table("room_members") {
    val roomId = uuid("room_id").references(StudyRooms.id)
    val userId = uuid("user_id").references(Users.id)
    val joinedAt = datetime("joined_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(roomId, userId)
}