package com.example.studysyncandroid.data.local.entities

import androidx.room.Embedded
import androidx.room.Relation

data class FolderWithDecks(
    @Embedded val folder: FolderEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "folderId"
    )
    val decks: List<DeckEntity>
)
