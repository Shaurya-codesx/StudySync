package com.example.repositories

import com.example.dto.DeckDetailResponse
import com.example.dto.DeckResponse
import com.example.models.Cards
import com.example.models.Decks
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class DeckRepository {

    // Fetch all decks for a specific user, counting the flashcards in each
    fun getAllForUser(userId: UUID): List<DeckResponse> = transaction {
        val cardCount = Cards.id.count()

        Decks.join(Cards, JoinType.LEFT, additionalConstraint = { Decks.id eq Cards.deckId })
            .slice(Decks.id, Decks.folderId, Decks.title, Decks.createdAt, cardCount)
            .select { Decks.userId eq userId }
            .groupBy(Decks.id, Decks.folderId, Decks.title, Decks.createdAt)
            .map {
                DeckResponse(
                    id = it[Decks.id].toString(),
                    folderId = it[Decks.folderId]?.toString(),
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

    // Update a deck's folder (only if the user owns the deck)
    fun updateDeckFolder(deckId: UUID, userId: UUID, folderId: UUID?): Boolean = transaction {
        val updatedCount = Decks.update({ (Decks.id eq deckId) and (Decks.userId eq userId) }) {
            it[Decks.folderId] = folderId
        }
        updatedCount > 0
    }

    // Fetch decks by a specific folder_id (or where folder_id is null)
    fun getByFolderIdAndUser(folderId: UUID?, userId: UUID): List<DeckResponse> = transaction {
        val cardCount = Cards.id.count()

        val condition = if (folderId == null) {
            (Decks.userId eq userId) and (Decks.folderId.isNull())
        } else {
            (Decks.userId eq userId) and (Decks.folderId eq folderId)
        }

        Decks.join(Cards, JoinType.LEFT, additionalConstraint = { Decks.id eq Cards.deckId })
            .slice(Decks.id, Decks.folderId, Decks.title, Decks.createdAt, cardCount)
            .select { condition }
            .groupBy(Decks.id, Decks.folderId, Decks.title, Decks.createdAt)
            .map {
                DeckResponse(
                    id = it[Decks.id].toString(),
                    folderId = it[Decks.folderId]?.toString(),
                    title = it[Decks.title],
                    cardCount = it[cardCount].toInt(),
                    createdAt = it[Decks.createdAt].toString()
                )
            }
    }
}