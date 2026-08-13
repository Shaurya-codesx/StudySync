package com.example.repositories

import com.example.models.Otps
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import org.jetbrains.exposed.sql.javatime.CurrentDateTime

class OtpRepository {

    fun insertOtp(emailInput: String, otpHashInput: String, expiresAtInput: LocalDateTime, typeInput: String) {
        transaction {
            // Lazy cleanup: delete any existing OTPs for this email before inserting a new one
            Otps.deleteWhere { Otps.email eq emailInput and (Otps.type eq typeInput) }
            
            Otps.insert {
                it[email] = emailInput
                it[otpHash] = otpHashInput
                it[expiresAt] = expiresAtInput
                it[type] = typeInput
            }
        }
    }

    fun findLatestOtp(emailInput: String, typeInput: String): ResultRow? {
        return transaction {
            Otps.select { Otps.email eq emailInput and (Otps.type eq typeInput) }
                .orderBy(Otps.createdAt to org.jetbrains.exposed.sql.SortOrder.DESC)
                .limit(1)
                .singleOrNull()
        }
    }

    fun deleteOtp(emailInput: String, typeInput: String) {
        transaction {
            Otps.deleteWhere { Otps.email eq emailInput and (Otps.type eq typeInput) }
        }
    }
}
