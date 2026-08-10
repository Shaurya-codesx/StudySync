package com.example.services

import com.example.dto.DeckDetailResponse
import com.example.dto.DeckResponse
import com.example.repositories.DeckRepository
import java.util.UUID

class DeckService {
    // Bring in the repository so we can talk to the database
    private val deckRepository = DeckRepository()

    // 1. Get all decks for the user
    fun getAllDecksForUser(userId: UUID): List<DeckResponse> {
        return deckRepository.getAllForUser(userId)
    }

    // 2. Get a specific deck, ensuring the user owns it
    fun getDeckDetails(deckId: UUID, userId: UUID): DeckDetailResponse? {
        return deckRepository.getByIdAndUser(deckId, userId)
    }

    // 3. Delete a deck securely
    fun deleteDeck(deckId: UUID, userId: UUID): Boolean {
        return deckRepository.deleteByIdAndUser(deckId, userId)
    }

    // 4. Create a deck manually and return it
    fun createDeck(userId: UUID, title: String): DeckResponse {
        val newDeckId = deckRepository.insertDeck(userId, title)
        // Fetch it back so we can return the complete DeckResponse (with cardCount and createdAt)
        return deckRepository.getAllForUser(userId).first { it.id == newDeckId.toString() }
    }

    // 5. Update a deck's folder
    fun updateDeckFolder(deckId: UUID, userId: UUID, folderId: UUID?): Boolean {
        return deckRepository.updateDeckFolder(deckId, userId, folderId)
    }

    // 6. Publish a deck
    fun publishDeck(deckId: UUID, userId: UUID): Boolean {
        return deckRepository.publishDeck(deckId, userId)
    }

    // 7. Unpublish a deck
    fun unpublishDeck(deckId: UUID, userId: UUID): Boolean {
        return deckRepository.unpublishDeck(deckId, userId)
    }

    // 8. Get public decks
    fun getPublicDecks(userId: UUID, page: Int, limit: Int): com.example.dto.PaginatedResponse<DeckResponse> {
        return deckRepository.getPublicDecks(userId, page, limit)
    }

    // 8. Clone a public deck
    fun clonePublicDeck(deckId: UUID, newUserId: UUID): DeckResponse {
        return deckRepository.clonePublicDeck(deckId, newUserId)
    }
}