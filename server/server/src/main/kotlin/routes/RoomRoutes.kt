package com.example.routes

import com.example.dto.CreateRoomRequest
import com.example.dto.CreateRoomResponse
import com.example.dto.EditRoomNameRequest
import com.example.dto.IncomingRoomEvent
import com.example.dto.JoinRoomResponse
import com.example.dto.RoomDetailsResponse
import com.example.dto.RoomMemberDto
import com.example.models.Users
import com.example.repositories.RoomRepository
import com.example.repositories.UserRepository
import com.example.services.RoomService
import com.example.utils.JwtUtils
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.json.Json
import java.util.UUID

private val incomingEventJson = Json { ignoreUnknownKeys = true }

fun Route.roomRoutes(
    roomRepository: RoomRepository,
    roomService: RoomService,
    userRepository: UserRepository
) {
    authenticate("jwt") {

        post("/rooms") {
            // NEW: Parse the incoming request for the room name
            val request = runCatching { call.receive<CreateRoomRequest>() }.getOrNull()
            if (request == null || request.name.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Room name is required"))
                return@post
            }

            val principal = call.principal<JWTPrincipal>()
            val userIdStr = principal?.payload?.getClaim("userId")?.asString()
            if (userIdStr == null) {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid or missing token"))
                return@post
            }
            val hostId = UUID.fromString(userIdStr)

            // NEW: Pass the name to the repository and return it in the response
            val room = roomRepository.createRoom(hostId, request.name)
            call.respond(HttpStatusCode.Created, CreateRoomResponse(roomId = room.id.toString(), code = room.code, name = room.name))
        }

        post("/rooms/{code}/join") {
            val code = call.parameters["code"]
            if (code == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing room code"))
                return@post
            }

            val principal = call.principal<JWTPrincipal>()
            val userIdStr = principal?.payload?.getClaim("userId")?.asString()
            if (userIdStr == null) {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid or missing token"))
                return@post
            }
            val userId = UUID.fromString(userIdStr)

            val room = roomRepository.findRoomByCode(code)
            if (room == null || !room.isActive) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Room not found or no longer active"))
                return@post
            }

            roomRepository.addMember(room.id, userId)
            val members = roomRepository.getMembers(room.id)

            call.respond(
                HttpStatusCode.OK,
                JoinRoomResponse(
                    roomId = room.id.toString(),
                    name = room.name, // NEW
                    members = members.map { RoomMemberDto(it.userId.toString(), it.displayName) }
                )
            )
        }

        get("/rooms/{code}") {
            val code = call.parameters["code"]
            if (code == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing room code"))
                return@get
            }

            val room = roomRepository.findRoomByCode(code)
            if (room == null || !room.isActive) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Room not found"))
                return@get
            }

            val members = roomRepository.getMembers(room.id)

            call.respond(
                HttpStatusCode.OK,
                RoomDetailsResponse(
                    roomId = room.id.toString(),
                    name = room.name, // NEW
                    hostId = room.hostId.toString(),
                    isActive = room.isActive,
                    members = members.map { RoomMemberDto(it.userId.toString(), it.displayName) }
                )
            )
        }

        // NEW: Edit room name route
        patch("/rooms/{code}/name") {
            val code = call.parameters["code"] ?: return@patch call.respond(HttpStatusCode.BadRequest)

            val request = runCatching { call.receive<EditRoomNameRequest>() }.getOrNull()
            if (request == null || request.name.isBlank()) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Name is required"))
                return@patch
            }

            val principal = call.principal<JWTPrincipal>()
            val userIdStr = principal?.payload?.getClaim("userId")?.asString()
            if (userIdStr == null) {
                call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                return@patch
            }
            val userId = UUID.fromString(userIdStr)

            val room = roomRepository.findRoomByCode(code)
            if (room == null || !room.isActive) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Room not found"))
                return@patch
            }

            // Only the host can rename the room
            if (room.hostId != userId) {
                call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Only the host can rename the room"))
                return@patch
            }

            // Update database
            roomRepository.updateRoomName(room.id, request.name)

            // Tell RoomService to broadcast the name change via WebSockets
            roomService.broadcastNameChange(code, request.name)

            call.respond(HttpStatusCode.OK, mapOf("success" to true))
        }
    }

    // Intentionally NOT wrapped in authenticate("jwt") — per the protocol spec, the token
    // travels as a query param on the WS handshake, so it's verified manually below.
    webSocket("/ws/rooms/{code}") {
        val code = call.parameters["code"]
        val token = call.request.queryParameters["token"]

        if (code == null || token == null) {
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Missing room code or token"))
            return@webSocket
        }

        val decoded = JwtUtils.verifyToken(token)
        val userIdStr = decoded?.getClaim("userId")?.asString()
        if (userIdStr == null) {
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Invalid or expired token"))
            return@webSocket
        }
        val userId = UUID.fromString(userIdStr)

        val room = roomRepository.findRoomByCode(code)
        if (room == null) {
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Room not found"))
            return@webSocket
        }

        // must have joined via REST first — the socket only handles live events, not membership
        if (!roomRepository.isMember(room.id, userId)) {
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Join the room via REST before connecting"))
            return@webSocket
        }

        val userRow = userRepository.findById(userId)
        val displayName = userRow?.get(Users.displayName) ?: "Anonymous"

        try {
            roomService.join(code, room.hostId, userId, displayName, this)

            for (frame in incoming) {
                if (frame is Frame.Text) {
                    val text = frame.readText()
                    val event = try {
                        incomingEventJson.decodeFromString(IncomingRoomEvent.serializer(), text)
                    } catch (e: Exception) {
                        null
                    }
                    when (event?.type) {
                        "timer_start" -> roomService.handleTimerStart(code, userId)
                        "timer_pause" -> roomService.handleTimerPause(code, userId)
                        // NEW: Handle host changing the time
                        "timer_update" -> {
                            if (event.durationSeconds != null) {
                                roomService.handleTimerUpdateDuration(code, userId, event.durationSeconds)
                            }
                        }
                    }
                }
            }
        } finally {
            val leaveResult = roomService.leave(code, userId)
            roomRepository.removeMember(room.id, userId)

            if (leaveResult.isRoomEmpty) {
                // HARD DELETE: Completely wipe the room from the database
                roomRepository.deleteRoom(room.id)
            } else if (leaveResult.newHostId != null) {
                // The host left but others remain, assign the new host
                roomRepository.updateHost(room.id, leaveResult.newHostId)
            }
        }
    }
}