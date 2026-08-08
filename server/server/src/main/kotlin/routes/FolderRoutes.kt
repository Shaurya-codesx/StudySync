package com.example.routes

import com.example.dto.CreateFolderRequest
import com.example.repositories.FolderRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.receive
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.UUID

fun Route.folderRoutes(folderRepository: FolderRepository) {
    authenticate("jwt") {
        route("/folders") {
            
            // 1. Get all folders for the logged-in user
            get {
                val principal = call.principal<JWTPrincipal>()
                val userIdStr = principal?.payload?.getClaim("userId")?.asString()

                if (userIdStr == null) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                    return@get
                }

                val userId = UUID.fromString(userIdStr)
                val folders = folderRepository.getFoldersByUser(userId)

                call.respond(HttpStatusCode.OK, folders)
            }

            // 2. Create a new folder
            post {
                val principal = call.principal<JWTPrincipal>()
                val userIdStr = principal?.payload?.getClaim("userId")?.asString()

                if (userIdStr == null) {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Invalid token"))
                    return@post
                }

                val userId = UUID.fromString(userIdStr)

                val request = try {
                    call.receive<CreateFolderRequest>()
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid JSON body"))
                    return@post
                }

                if (request.name.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Folder name cannot be empty"))
                    return@post
                }

                val newFolder = folderRepository.createFolder(userId, request.name)
                call.respond(HttpStatusCode.Created, newFolder)
            }

            // 3. Delete a folder
            delete("{id}") {
                val principal = call.principal<JWTPrincipal>()
                val userIdStr = principal?.payload?.getClaim("userId")?.asString()
                val folderIdStr = call.parameters["id"]

                if (userIdStr == null || folderIdStr == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing data"))
                    return@delete
                }

                val userId = UUID.fromString(userIdStr)
                val folderId = try {
                    UUID.fromString(folderIdStr)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Invalid Folder ID format"))
                    return@delete
                }

                val deleted = folderRepository.deleteFolder(folderId, userId)

                if (deleted) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Folder not found or access denied"))
                }
            }
        }
    }
}
