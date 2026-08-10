package com.example.repositories

import com.example.dto.DeckDetailResponse
import com.example.dto.DeckResponse
import com.example.models.Cards
import com.example.models.Decks
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.neq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class DeckRepository {

    // Fetch all decks for a specific user, counting the flashcards in each
    fun getAllForUser(userId: UUID): List<DeckResponse> = transaction {
        val cardCount = Cards.id.count()

        Decks.join(Cards, JoinType.LEFT, additionalConstraint = { Decks.id eq Cards.deckId })
            .slice(Decks.id, Decks.folderId, Decks.title, Decks.isPublic, Decks.createdAt, cardCount)
            .select { Decks.userId eq userId }
            .groupBy(Decks.id, Decks.folderId, Decks.title, Decks.isPublic, Decks.createdAt)
            .map {
                DeckResponse(
                    id = it[Decks.id].toString(),
                    folderId = it[Decks.folderId]?.toString(),
                    title = it[Decks.title],
                    cardCount = it[cardCount].toInt(),
                    isPublic = it[Decks.isPublic],
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
            it[Decks.isPublic] = false
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
            .slice(Decks.id, Decks.folderId, Decks.title, Decks.isPublic, Decks.createdAt, cardCount)
            .select { condition }
            .groupBy(Decks.id, Decks.folderId, Decks.title, Decks.isPublic, Decks.createdAt)
            .map {
                DeckResponse(
                    id = it[Decks.id].toString(),
                    folderId = it[Decks.folderId]?.toString(),
                    title = it[Decks.title],
                    cardCount = it[cardCount].toInt(),
                    isPublic = it[Decks.isPublic],
                    createdAt = it[Decks.createdAt].toString()
                )
            }
    }

    // Clone a public deck
    fun clonePublicDeck(deckId: UUID, newUserId: UUID): DeckResponse = transaction {
        // 1. Fetch original deck ensuring it is public
        val originalDeck = Decks.select { (Decks.id eq deckId) and (Decks.isPublic eq true) }.singleOrNull()
            ?: throw IllegalArgumentException("Deck not found or not public")

        val newDeckTitle = originalDeck[Decks.title]
        val newSourceText = originalDeck[Decks.sourceText]

        // 2. Create a new Deck row for newUserId
        val insertDeckStatement = Decks.insert {
            it[userId] = newUserId
            it[title] = newDeckTitle
            it[sourceText] = newSourceText
            it[folderId] = null
            it[isPublic] = false
        }
        val newDeckId = insertDeckStatement[Decks.id]

        // 3. Fetch all Cards associated with the original deckId
        val originalCards = Cards.select { Cards.deckId eq deckId }.toList()

        // 4. Batch insert new Card rows linking to the newly created deck's ID
        if (originalCards.isNotEmpty()) {
            Cards.batchInsert(originalCards) { originalCard ->
                this[Cards.deckId] = newDeckId
                this[Cards.question] = originalCard[Cards.question]
                this[Cards.answer] = originalCard[Cards.answer]
                // Resetting ease_factor, interval_days, and repetitions to their defaults
                this[Cards.easeFactor] = 2.5f
                this[Cards.intervalDays] = 0
                this[Cards.repetitions] = 0
            }
        }
        
        // 5. Fetch the newly inserted deck to get the generated createdAt timestamp
        val newDeckRow = Decks.select { Decks.id eq newDeckId }.single()

        // Return the new deck response
        DeckResponse(
            id = newDeckId.toString(),
            folderId = null,
            title = newDeckTitle,
            cardCount = originalCards.size,
            isPublic = false,
            createdAt = newDeckRow[Decks.createdAt].toString()
        )
    }

    // Publish a deck
    fun publishDeck(deckId: UUID, userId: UUID): Boolean = transaction {
        val updatedCount = Decks.update({ (Decks.id eq deckId) and (Decks.userId eq userId) }) {
            it[isPublic] = true
        }
        updatedCount > 0
    }

    // Unpublish a deck
    fun unpublishDeck(deckId: UUID, userId: UUID): Boolean = transaction {
        val updatedCount = Decks.update({ (Decks.id eq deckId) and (Decks.userId eq userId) }) {
            it[isPublic] = false
        }
        updatedCount > 0
    }

    // Get paginated public decks, excluding the user's own decks
    fun getPublicDecks(userId: UUID, page: Int, limit: Int): com.example.dto.PaginatedResponse<DeckResponse> = transaction {
        val condition = (Decks.isPublic eq true) and (Decks.userId neq userId)
        
        val totalItems = Decks.select { condition }.count()
        val totalPages = Math.ceil(totalItems.toDouble() / limit).toInt()

        val cardCount = Cards.id.count()
        
        val items = Decks.join(Cards, JoinType.LEFT, additionalConstraint = { Decks.id eq Cards.deckId })
            .slice(Decks.id, Decks.folderId, Decks.title, Decks.isPublic, Decks.createdAt, cardCount)
            .select { condition }
            .groupBy(Decks.id, Decks.folderId, Decks.title, Decks.isPublic, Decks.createdAt)
            .orderBy(Decks.createdAt to SortOrder.DESC)
            .limit(limit, offset = ((page - 1) * limit).toLong())
            .map {
                DeckResponse(
                    id = it[Decks.id].toString(),
                    folderId = it[Decks.folderId]?.toString(),
                    title = it[Decks.title],
                    cardCount = it[cardCount].toInt(),
                    isPublic = it[Decks.isPublic],
                    createdAt = it[Decks.createdAt].toString()
                )
            }

        com.example.dto.PaginatedResponse(
            items = items,
            page = page,
            totalPages = totalPages,
            totalItems = totalItems
        )
    }
}