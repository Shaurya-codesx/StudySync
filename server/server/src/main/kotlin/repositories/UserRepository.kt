package com.example.repositories

import com.example.models.Users
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class UserRepository {

    // 1. Insert User (Roadmap Requirement)
    fun insertUser(emailInput: String, passwordHashInput: String, displayNameInput: String): UUID? {
        return transaction {
            val insertStatement = Users.insert {
                it[email] = emailInput
                it[passwordHash] = passwordHashInput
                it[displayName] = displayNameInput
            }
            insertStatement[Users.id]
        }
    }

    // 2. Find by Email (Roadmap Requirement)
    fun findByEmail(emailInput: String): ResultRow? {
        return transaction {
            // Replaced selectAll().where with just select
            Users.select { Users.email eq emailInput }.singleOrNull()
        }
    }

    // 3. Find by ID (For refreshing tokens)
    fun findById(idInput: UUID): ResultRow? {
        return transaction {
            Users.select { Users.id eq idInput }.singleOrNull()
        }
    }

    fun verifyUser(emailInput: String) {
        transaction {
            Users.update({ Users.email eq emailInput }) {
                it[isVerified] = true
            }
        }
    }

    fun updatePassword(emailInput: String, newPasswordHash: String) {
        transaction {
            Users.update({ Users.email eq emailInput }) {
                it[passwordHash] = newPasswordHash
            }
        }
    }

    fun updateDisplayName(idInput: UUID, newName: String) {
        transaction {
            Users.update({ Users.id eq idInput }) {
                it[displayName] = newName
            }
        }
    }

    fun deleteUser(userIdInput: UUID) {
        transaction {
            // Because Exposed lacks CASCADE for some references, we manually delete children
            com.example.models.RoomMembers.deleteWhere { userId eq userIdInput }
            com.example.models.StudyRooms.deleteWhere { hostId eq userIdInput }
            com.example.models.Decks.deleteWhere { userId eq userIdInput }
            com.example.models.Folders.deleteWhere { userId eq userIdInput }
            Users.deleteWhere { id eq userIdInput }
        }
    }
}