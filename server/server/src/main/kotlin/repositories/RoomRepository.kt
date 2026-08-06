package com.example.repositories

import com.example.models.RoomMembers
import com.example.models.StudyRooms
import com.example.models.Users
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

data class RoomInfo(
    val id: UUID,
    val code: String,
    val name: String, // NEW
    val hostId: UUID,
    val isActive: Boolean
)

data class RoomMemberInfo(
    val userId: UUID,
    val displayName: String
)

class RoomRepository {

    // Excludes visually ambiguous characters (0/O, 1/I) so codes are easy to read aloud/type
    private val codeChars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

    private fun generateUniqueCode(): String {
        while (true) {
            val candidate = (1..6).map { codeChars.random() }.joinToString("")
            val exists = transaction {
                StudyRooms.select { StudyRooms.code eq candidate }.singleOrNull() != null
            }
            if (!exists) return candidate
        }
    }

    fun createRoom(hostId: UUID, roomName: String): RoomInfo {
        val code = generateUniqueCode()
        return transaction {
            val insertStatement = StudyRooms.insert {
                it[StudyRooms.code] = code
                it[StudyRooms.name] = roomName // NEW
                it[StudyRooms.hostId] = hostId
            }
            val roomId = insertStatement[StudyRooms.id]

            RoomMembers.insert {
                it[RoomMembers.roomId] = roomId
                it[RoomMembers.userId] = hostId
            }

            RoomInfo(id = roomId, code = code, name = roomName, hostId = hostId, isActive = true)
        }
    }

    fun findRoomByCode(code: String): RoomInfo? = transaction {
        StudyRooms.select { StudyRooms.code eq code }
            .map {
                RoomInfo(
                    id = it[StudyRooms.id],
                    code = it[StudyRooms.code],
                    name = it[StudyRooms.name], // NEW
                    hostId = it[StudyRooms.hostId],
                    isActive = it[StudyRooms.isActive]
                )
            }
            .singleOrNull()
    }

    fun updateRoomName(roomId: UUID, newName: String) = transaction {
        StudyRooms.update({ StudyRooms.id eq roomId }) {
            it[name] = newName
        }
    }

    fun addMember(roomId: UUID, userId: UUID) = transaction {
        val alreadyMember = RoomMembers.select {
            (RoomMembers.roomId eq roomId) and (RoomMembers.userId eq userId)
        }.singleOrNull() != null

        if (!alreadyMember) {
            RoomMembers.insert {
                it[RoomMembers.roomId] = roomId
                it[RoomMembers.userId] = userId
            }
        }
    }

    fun updateHost(roomId: UUID, newHostId: UUID) = transaction {
        StudyRooms.update({ StudyRooms.id eq roomId }) {
            it[hostId] = newHostId
        }
    }

    fun deactivateRoom(roomId: UUID) = transaction {
        StudyRooms.update({ StudyRooms.id eq roomId }) {
            it[isActive] = false
        }
    }

    fun isMember(roomId: UUID, userId: UUID): Boolean = transaction {
        RoomMembers.select {
            (RoomMembers.roomId eq roomId) and (RoomMembers.userId eq userId)
        }.singleOrNull() != null
    }

    fun getMembers(roomId: UUID): List<RoomMemberInfo> = transaction {
        (RoomMembers innerJoin Users)
            .select { RoomMembers.roomId eq roomId }
            .map {
                RoomMemberInfo(
                    userId = it[Users.id],
                    displayName = it[Users.displayName] ?: "Anonymous"
                )
            }
    }

    fun removeMember(roomId: UUID, userId: UUID) = transaction {
        RoomMembers.deleteWhere {
            (RoomMembers.roomId eq roomId) and (RoomMembers.userId eq userId)
        }
    }

    fun deleteRoom(roomId: UUID) = transaction {
        StudyRooms.deleteWhere { StudyRooms.id eq roomId }
    }
}