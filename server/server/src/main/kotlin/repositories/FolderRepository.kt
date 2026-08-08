package com.example.repositories

import com.example.dto.FolderResponse
import com.example.models.Decks
import com.example.models.Folders
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class FolderRepository {
    fun createFolder(userId: UUID, name: String): FolderResponse = transaction {
        val insertStatement = Folders.insert {
            it[Folders.userId] = userId
            it[Folders.name] = name
        }
        val id = insertStatement[Folders.id]
        
        Folders.select { Folders.id eq id }.map {
            FolderResponse(
                id = it[Folders.id].toString(),
                name = it[Folders.name],
                createdAt = it[Folders.createdAt].toString()
            )
        }.single()
    }

    fun getFoldersByUser(userId: UUID): List<FolderResponse> = transaction {
        Folders.select { Folders.userId eq userId }.map {
            FolderResponse(
                id = it[Folders.id].toString(),
                name = it[Folders.name],
                createdAt = it[Folders.createdAt].toString()
            )
        }
    }

    fun deleteFolder(folderId: UUID, userId: UUID): Boolean = transaction {
        // Only delete if the user owns it
        val folderExists = Folders.select { (Folders.id eq folderId) and (Folders.userId eq userId) }.singleOrNull() != null
        if (folderExists) {
            val deletedCount = Folders.deleteWhere { (Folders.id eq folderId) and (Folders.userId eq userId) }
            deletedCount > 0
        } else {
            false
        }
    }
}
