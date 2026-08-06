package com.example.studysyncandroid.ui.rooms

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.studysyncandroid.data.remote.RoomLiveState

@Composable
fun RoomsScreen(
    viewModel: RoomViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val liveState by viewModel.liveState.collectAsState()

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.currentRoomCode == null) {
                // Lobby State: Create or Join
                LobbyContent(
                    isLoading = uiState.isLoading,
                    error = uiState.error,
                    onCreateRoom = { name -> viewModel.createRoom(name) },
                    onJoinRoom = { code -> viewModel.joinRoom(code) }
                )
            } else {
                // Active Room State
                ActiveRoomContent(
                    roomCode = uiState.currentRoomCode!!,
                    liveState = liveState,
                    onStartTimer = { viewModel.startTimer() },
                    onPauseTimer = { viewModel.pauseTimer() },
                    onChangeDuration = { minutes -> viewModel.changeTimerDuration(minutes) },
                    onRenameRoom = { newName -> viewModel.renameRoom(newName) },
                    onLeaveRoom = { viewModel.leaveRoom() }
                )
            }
        }
    }
}

@Composable
private fun LobbyContent(
    isLoading: Boolean,
    error: String?,
    onCreateRoom: (String) -> Unit,
    onJoinRoom: (String) -> Unit
) {
    var roomName by remember { mutableStateOf("") }
    var joinCode by remember { mutableStateOf("") }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Study Rooms",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // CREATE ROOM SECTION
        OutlinedTextField(
            value = roomName,
            onValueChange = { roomName = it },
            label = { Text("New Room Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { onCreateRoom(roomName) },
            enabled = !isLoading && roomName.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Create New Room")
        }

        Text(
            text = "— OR —",
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(vertical = 24.dp)
        )

        // JOIN ROOM SECTION
        OutlinedTextField(
            value = joinCode,
            onValueChange = { joinCode = it.uppercase() },
            label = { Text("Enter 6-Digit Code") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = { onJoinRoom(joinCode) },
            enabled = !isLoading && joinCode.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Join Room")
        }

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 24.dp))
        }

        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

@Composable
private fun ActiveRoomContent(
    roomCode: String,
    liveState: RoomLiveState,
    onStartTimer: () -> Unit,
    onPauseTimer: () -> Unit,
    onChangeDuration: (Int) -> Unit,
    onRenameRoom: (String) -> Unit,
    onLeaveRoom: () -> Unit
) {
    var showRenameDialog by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        // HEADER: Room Name & Code
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = liveState.roomName.ifEmpty { "Study Room" },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            if (liveState.isHost) {
                IconButton(onClick = { showRenameDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Room Name",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Text(
            text = "Code: $roomCode",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = if (liveState.isConnected) "Status: Connected (Live)" else "Status: Reconnecting...",
            color = if (liveState.isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // TIMER CARD
        val minutes = liveState.remainingSeconds / 60
        val seconds = liveState.remainingSeconds % 60
        val timerText = String.format(java.util.Locale.US, "%02d:%02d", minutes, seconds)
        val isRunning = liveState.timerState == "running"

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp).fillMaxWidth()
            ) {
                Text(
                    text = timerText,
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (liveState.isHost) {
                    Button(
                        onClick = { if (isRunning) onPauseTimer() else onStartTimer() },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        Text(if (isRunning) "Pause Timer" else "Start Timer")
                    }

                    // Custom duration chips (only visible to host when paused)
                    if (!isRunning) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Set Timer:", style = MaterialTheme.typography.labelMedium)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            FilterChip(selected = false, onClick = { onChangeDuration(15) }, label = { Text("15m") })
                            FilterChip(selected = false, onClick = { onChangeDuration(25) }, label = { Text("25m") })
                            FilterChip(selected = false, onClick = { onChangeDuration(50) }, label = { Text("50m") })
                        }
                    }
                } else {
                    Text(
                        text = "The host controls the timer.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // MEMBERS LIST
        Text(
            text = "Members (${liveState.members.size})",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth()
        ) {
            items(liveState.members) { member ->
                ListItem(
                    headlineContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(member.displayName)
                            if (member.userId == liveState.hostId) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "👑 Host",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    },
                    supportingContent = {
                        Text(
                            text = if (member.userId == liveState.currentUserId) "You" else member.userId.take(8) + "...",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                )
                HorizontalDivider()
            }
        }

        OutlinedButton(
            onClick = onLeaveRoom,
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            Text("Leave Room", color = MaterialTheme.colorScheme.error)
        }
    }

    // RENAME DIALOG
    if (showRenameDialog) {
        var editNameInput by remember { mutableStateOf(liveState.roomName) }
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Room") },
            text = {
                OutlinedTextField(
                    value = editNameInput,
                    onValueChange = { editNameInput = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (editNameInput.isNotBlank()) {
                            onRenameRoom(editNameInput)
                        }
                        showRenameDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}