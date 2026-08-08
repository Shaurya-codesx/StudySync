package com.example.studysyncandroid.data.repository

import com.example.studysyncandroid.data.local.dao.FolderDao
import com.example.studysyncandroid.data.local.entities.FolderEntity
import com.example.studysyncandroid.data.local.entities.FolderWithDecks
import com.example.studysyncandroid.data.remote.FolderApi
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FolderRepository @Inject constructor(
    private val folderApi: FolderApi,
    private val folderDao: FolderDao
) {
    fun getFoldersStream(): Flow<List<FolderEntity>> = folderDao.getAllFolders()

    fun getFoldersWithDecksStream(): Flow<List<FolderWithDecks>> = folderDao.getFoldersWithDecks()

    suspend fun refreshFolders(): Result<Unit> = runCatching {
        val folders = folderApi.getFolders()
        folderDao.insertFolders(
            folders.map { response ->
                FolderEntity(
                    id = response.id,
                    name = response.name,
                    createdAt = response.createdAt
                )
            }
        )
    }

    suspend fun createFolder(name: String): Result<String> = runCatching {
        val response = folderApi.createFolder(name)
        folderDao.insertFolder(
            FolderEntity(
                id = response.id,
                name = response.name,
                createdAt = response.createdAt
            )
        )
        response.id
    }

    suspend fun deleteFolder(id: String): Result<Unit> = runCatching {
        folderApi.deleteFolder(id)
        folderDao.deleteFolder(id)
    }
}
