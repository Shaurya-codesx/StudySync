package com.example.repositories

import com.example.dto.CardResponse
import com.example.models.Cards
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class CardRepository {

    // Fetch all cards for a specific deck
    fun getCardsForDeck(deckId: UUID): List<CardResponse> = transaction {
        Cards.select { Cards.deckId eq deckId }
            .map {
                CardResponse(
                    id = it[Cards.id].toString(),
                    question = it[Cards.question],
                    answer = it[Cards.answer],
                    dueDate = it[Cards.dueDate].toString()
                )
            }
    }
}