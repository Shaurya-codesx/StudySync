package com.example.services

import com.example.models.Users
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt

class UserService {

    fun createUser(usernameInput: String, passwordHashInput: String): Int? {

        // 1. Scramble the password using Bcrypt with a generated "salt"
        val scrambledPassword = BCrypt.hashpw(passwordHashInput, BCrypt.gensalt())

        return transaction {
            val insertStatement = Users.insert {
                it[username] = usernameInput
                // 2. Save the scrambled string to the database instead of the raw text
                it[passwordHash] = scrambledPassword
            }
            insertStatement[Users.id]
        }
    }
}