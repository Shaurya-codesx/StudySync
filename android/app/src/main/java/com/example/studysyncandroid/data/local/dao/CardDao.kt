package com.example.studysyncandroid.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.studysyncandroid.data.local.entities.CardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {

    @Query("SELECT * FROM cards WHERE deckId = :deckId")
    fun getCardsForDeck(deckId: String): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards WHERE deckId = :deckId AND dueDate <= :nowIso")
    suspend fun getDueCards(deckId: String, nowIso: String): List<CardEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: CardEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCards(cards: List<CardEntity>)

    @Update
    suspend fun updateCard(card: CardEntity)

    @Query("UPDATE cards SET dueDate = :newDueDate WHERE id = :cardId")
    suspend fun updateCardDueDate(cardId: String, newDueDate: String)
}