package com.example.utils

import org.mindrot.jbcrypt.BCrypt

object PasswordUtils {

    // Scrambles a raw password into a secure hash
    fun hashPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }

    // Compares a raw login password against the saved database hash
    fun verifyPassword(password: String, hashed: String): Boolean {
        return BCrypt.checkpw(password, hashed)
    }
}