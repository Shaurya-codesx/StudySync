package com.example.studysyncandroid.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "decks",
    foreignKeys = [
        ForeignKey(
            entity = FolderEntity::class,
            parentColumns = ["id"],
            childColumns = ["folderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("folderId")]
)
data class DeckEntity(
    @PrimaryKey val id: String,
    val folderId: String? = null,
    val title: String,
    val cardCount: Int,
    val isPublic: Boolean = false,
    val createdAt: String
)