package com.example.repositories

import com.example.models.Users
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class UserRepository {

    // 1. Insert User (Roadmap Requirement)
    fun insertUser(emailInput: String, passwordHashInput: String, displayNameInput: String): Int? {
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
    fun findById(idInput: Int): ResultRow? {
        return transaction {
            Users.select { Users.id eq idInput }.singleOrNull()
        }
    }
}