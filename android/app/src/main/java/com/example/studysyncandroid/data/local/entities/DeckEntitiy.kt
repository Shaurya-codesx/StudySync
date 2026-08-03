package com.example.studysyncandroid.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "decks")
data class DeckEntity(
    @PrimaryKey val id: String,
    val title: String,
    val cardCount: Int,
    val createdAt: String
)