package com.example.studysyncandroid.di

import android.content.Context
import androidx.room.Room
import com.example.studysyncandroid.data.local.AppDatabase
import com.example.studysyncandroid.data.local.dao.CardDao
import com.example.studysyncandroid.data.local.dao.DeckDao
import com.example.studysyncandroid.data.local.dao.FolderDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "studysync.db")
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun provideDeckDao(database: AppDatabase): DeckDao = database.deckDao()

    @Provides
    fun provideCardDao(database: AppDatabase): CardDao = database.cardDao()

    @Provides
    fun provideFolderDao(database: AppDatabase): FolderDao = database.folderDao()
}