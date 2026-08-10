package com.example.studysyncandroid.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.studysyncandroid.data.local.dao.CardDao
import com.example.studysyncandroid.data.local.dao.DeckDao
import com.example.studysyncandroid.data.local.entities.CardEntity
import com.example.studysyncandroid.data.local.entities.DeckEntity
import com.example.studysyncandroid.data.local.entities.FolderEntity
import com.example.studysyncandroid.data.local.dao.FolderDao

@Database(
    entities = [DeckEntity::class, CardEntity::class, FolderEntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun deckDao(): DeckDao
    abstract fun cardDao(): CardDao
    abstract fun folderDao(): FolderDao
}