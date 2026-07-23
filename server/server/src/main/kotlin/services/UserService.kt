package com.example.services

import com.example.models.Users
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

class UserService {

    // This function takes a username and password, inserts them into the database,
    // and returns the newly generated ID.
    fun createUser(usernameInput: String, passwordHashInput: String): Int? {
        return transaction {
            val insertStatement = Users.insert {
                it[username] = usernameInput
                it[passwordHash] = passwordHashInput
            }
            insertStatement[Users.id]
        }
    }
}