package com.example.studysyncandroid.ui.rooms

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
                    onCreateRoom = { viewModel.createRoom() },
                    onJoinRoom = { code -> viewModel.joinRoom(code) }
                )
            } else {
                // Active Room State
                ActiveRoomContent(
                    roomCode = uiState.currentRoomCode!!,
                    liveState = liveState,
                    onToggleTimer = { viewModel.toggleTimer() },
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
    onCreateRoom: () -> Unit,
    onJoinRoom: (String) -> Unit
) {
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

        Button(
            onClick = onCreateRoom,
            enabled = !isLoading,
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
            label = { Text("Enter Room Code") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

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
    onToggleTimer: () -> Unit,
    onLeaveRoom: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Room: $roomCode",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = if (liveState.isConnected) "Status: Connected (Live)" else "Status: Reconnecting...",
            color = if (liveState.isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Timer Display
        val minutes = liveState.remainingSeconds / 60
        val seconds = liveState.remainingSeconds % 60
        val timerText = String.format(java.util.Locale.US, "%02d:%02d", minutes, seconds)

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
                Button(onClick = onToggleTimer) {
                    Text(if (liveState.timerState == "running") "Pause Timer" else "Start Timer")
                }
            }
        }

        // Members List
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
                    headlineContent = { Text(member.displayName) },
                    supportingContent = { Text(member.userId, style = MaterialTheme.typography.labelSmall) }
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
}