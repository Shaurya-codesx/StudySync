package com.example.studysyncandroid.ui.rooms

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions // NEW IMPORT
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType // NEW IMPORT
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
                LobbyContent(
                    isLoading = uiState.isLoading,
                    error = uiState.error,
                    onCreateRoom = { name -> viewModel.createRoom(name) },
                    onJoinRoom = { code -> viewModel.joinRoom(code) }
                )
            } else {
                ActiveRoomContent(
                    roomCode = uiState.currentRoomCode!!,
                    liveState = liveState,
                    onStartTimer = { viewModel.startTimer() },
                    onPauseTimer = { viewModel.pauseTimer() },
                    // NEW: Pass both minutes and mode
                    onChangeDuration = { minutes, mode -> viewModel.changeTimerDuration(minutes, mode) },
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
    onChangeDuration: (Int, String) -> Unit, // NEW: Accepts mode string
    onRenameRoom: (String) -> Unit,
    onLeaveRoom: () -> Unit
) {
    var showRenameDialog by remember { mutableStateOf(false) }
    var showLeaveDialog by remember { mutableStateOf(false) }

    // NEW: States for Custom Time Dialog
    var showCustomTimeDialog by remember { mutableStateOf(false) }
    var isCustomForBreak by remember { mutableStateOf(false) }

    BackHandler {
        showLeaveDialog = true
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        // HEADER
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
                        imageVector = Icons.Filled.Edit,
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
        val isBreak = liveState.mode == "break"

        // NEW: Dynamic colors based on Work vs Break
        val cardContainerColor = if (isBreak) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant
        val cardContentColor = if (isBreak) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            colors = CardDefaults.cardColors(
                containerColor = cardContainerColor,
                contentColor = cardContentColor
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp).fillMaxWidth()
            ) {
                // NEW: Show "Study Time" or "Break Time"
                Text(
                    text = if (isBreak) "☕ Break Time" else "📚 Study Time",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = cardContentColor
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = timerText,
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = cardContentColor
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (liveState.isHost) {
                    Button(
                        onClick = { if (isRunning) onPauseTimer() else onStartTimer() },
                        modifier = Modifier.fillMaxWidth(0.8f),
                        colors = if (isBreak) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary) else ButtonDefaults.buttonColors()
                    ) {
                        Text(if (isRunning) "Pause Timer" else "Start Timer")
                    }

                    if (!isRunning) {
                        Spacer(modifier = Modifier.height(16.dp))

                        // STUDY SETTINGS
                        Text("Set Study Time:", style = MaterialTheme.typography.labelMedium, color = cardContentColor)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(selected = !isBreak && minutes == 25, onClick = { onChangeDuration(25, "work") }, label = { Text("25m") })
                            FilterChip(selected = !isBreak && minutes == 50, onClick = { onChangeDuration(50, "work") }, label = { Text("50m") })
                            FilterChip(selected = false, onClick = {
                                isCustomForBreak = false
                                showCustomTimeDialog = true
                            }, label = { Text("+ Custom") })
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // BREAK SETTINGS
                        Text("Set Break Time:", style = MaterialTheme.typography.labelMedium, color = cardContentColor)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(selected = isBreak && minutes == 5, onClick = { onChangeDuration(5, "break") }, label = { Text("5m") })
                            FilterChip(selected = isBreak && minutes == 10, onClick = { onChangeDuration(10, "break") }, label = { Text("10m") })
                            FilterChip(selected = false, onClick = {
                                isCustomForBreak = true
                                showCustomTimeDialog = true
                            }, label = { Text("+ Custom") })
                        }
                    }
                } else {
                    Text(
                        text = "The host controls the timer.",
                        color = cardContentColor,
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
            onClick = { showLeaveDialog = true },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            Text("Leave Room", color = MaterialTheme.colorScheme.error)
        }
    }

    // LEAVE CONFIRMATION DIALOG
    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            title = { Text("Leave Room?") },
            text = { Text("Are you sure you want to leave this study room? You will need the 6-digit code to rejoin.") },
            confirmButton = {
                TextButton(onClick = {
                    showLeaveDialog = false
                    onLeaveRoom()
                }) {
                    Text("Leave", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveDialog = false }) { Text("Cancel") }
            }
        )
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
                TextButton(onClick = {
                    if (editNameInput.isNotBlank()) onRenameRoom(editNameInput)
                    showRenameDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) { Text("Cancel") }
            }
        )
    }

    // NEW: CUSTOM TIME DIALOG
    if (showCustomTimeDialog) {
        var customMinutes by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCustomTimeDialog = false },
            title = { Text(if (isCustomForBreak) "Custom Break Time" else "Custom Study Time") },
            text = {
                OutlinedTextField(
                    value = customMinutes,
                    // Only allow numbers to be typed
                    onValueChange = { if (it.all { char -> char.isDigit() }) customMinutes = it },
                    label = { Text("Minutes") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val mins = customMinutes.toIntOrNull()
                    if (mins != null && mins in 1..999) {
                        onChangeDuration(mins, if (isCustomForBreak) "break" else "work")
                    }
                    showCustomTimeDialog = false
                }) { Text("Set") }
            },
            dismissButton = {
                TextButton(onClick = { showCustomTimeDialog = false }) { Text("Cancel") }
            }
        )
    }
}