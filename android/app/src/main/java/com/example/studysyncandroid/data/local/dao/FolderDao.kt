package com.example.studysyncandroid.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.studysyncandroid.data.local.entities.FolderEntity
import com.example.studysyncandroid.data.local.entities.FolderWithDecks
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: FolderEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolders(folders: List<FolderEntity>)

    @Query("DELETE FROM folders WHERE id = :folderId")
    suspend fun deleteFolder(folderId: String)

    @Query("SELECT * FROM folders")
    fun getAllFolders(): Flow<List<FolderEntity>>

    @Transaction
    @Query("SELECT * FROM folders")
    fun getFoldersWithDecks(): Flow<List<FolderWithDecks>>
}
