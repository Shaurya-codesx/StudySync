package com.example.repositories

import com.example.dto.DeckDetailResponse
import com.example.dto.DeckResponse
import com.example.models.Cards
import com.example.models.Decks
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class DeckRepository {

    // Fetch all decks for a specific user, counting the flashcards in each
    fun getAllForUser(userId: UUID): List<DeckResponse> = transaction {
        val cardCount = Cards.id.count()

        Decks.join(Cards, JoinType.LEFT, additionalConstraint = { Decks.id eq Cards.deckId })
            .slice(Decks.id, Decks.title, Decks.createdAt, cardCount)
            .select { Decks.userId eq userId }
            .groupBy(Decks.id, Decks.title, Decks.createdAt)
            .map {
                DeckResponse(
                    id = it[Decks.id].toString(),
                    title = it[Decks.title],
                    cardCount = it[cardCount].toInt(),
                    createdAt = it[Decks.createdAt].toString()
                )
            }
    }

    // Fetch a single deck's details, ensuring the user actually owns it
    fun getByIdAndUser(deckId: UUID, userId: UUID): DeckDetailResponse? = transaction {
        Decks.select { (Decks.id eq deckId) and (Decks.userId eq userId) }
            .map {
                DeckDetailResponse(
                    id = it[Decks.id].toString(),
                    title = it[Decks.title],
                    sourceText = it[Decks.sourceText],
                    createdAt = it[Decks.createdAt].toString()
                )
            }
            .singleOrNull()
    }

    // Delete a deck (only if the user owns it)
    fun deleteByIdAndUser(deckId: UUID, userId: UUID): Boolean = transaction {
        val deletedCount = Decks.deleteWhere { (Decks.id eq deckId) and (Decks.userId eq userId) }
        deletedCount > 0
    }

    // Create a new deck manually
    fun insertDeck(userId: UUID, title: String): UUID = transaction {
        val insertStatement = Decks.insert {
            it[Decks.userId] = userId
            it[Decks.title] = title
            it[Decks.sourceText] = null // Null for now, will be used in Phase 6
        }
        insertStatement[Decks.id]
    }
}