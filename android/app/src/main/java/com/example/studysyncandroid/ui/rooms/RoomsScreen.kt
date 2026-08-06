package com.example.studysyncandroid.ui.rooms

import android.content.Context
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle // NEW
import androidx.compose.ui.text.style.TextDecoration // NEW
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.studysyncandroid.data.remote.RoomLiveState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.ImeAction

@Composable
fun RoomsScreen(
    viewModel: RoomViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val liveState by viewModel.liveState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
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
                    snackbarHostState = snackbarHostState,
                    onStartTimer = { viewModel.startTimer() },
                    onPauseTimer = { viewModel.pauseTimer() },
                    onChangeDuration = { minutes, mode -> viewModel.changeTimerDuration(minutes, mode) },
                    onRenameRoom = { newName -> viewModel.renameRoom(newName) },
                    onLeaveRoom = { viewModel.leaveRoom() },
                    // NEW: Pass the ViewModel function
                    onUpdateTask = { task, isDone -> viewModel.updateMyTask(task, isDone) }
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
        Text("Study Rooms", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 32.dp))
        OutlinedTextField(value = roomName, onValueChange = { roomName = it }, label = { Text("New Room Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { onCreateRoom(roomName) }, enabled = !isLoading && roomName.isNotBlank(), modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("Create New Room") }
        Text("— OR —", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(vertical = 24.dp))
        OutlinedTextField(value = joinCode, onValueChange = { joinCode = it.uppercase() }, label = { Text("Enter 6-Digit Code") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { onJoinRoom(joinCode) }, enabled = !isLoading && joinCode.isNotBlank(), modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("Join Room") }

        if (isLoading) CircularProgressIndicator(modifier = Modifier.padding(top = 24.dp))
        if (error != null) Text(error, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 16.dp))
    }
}

@Composable
private fun ActiveRoomContent(
    roomCode: String,
    liveState: RoomLiveState,
    snackbarHostState: SnackbarHostState,
    onStartTimer: () -> Unit,
    onPauseTimer: () -> Unit,
    onChangeDuration: (Int, String) -> Unit,
    onRenameRoom: (String) -> Unit,
    onLeaveRoom: () -> Unit,
    onUpdateTask: (String, Boolean) -> Unit // NEW PARAMETER
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val focusManager = LocalFocusManager.current

    var showRenameDialog by remember { mutableStateOf(false) }
    var showLeaveDialog by remember { mutableStateOf(false) }
    var showCustomTimeDialog by remember { mutableStateOf(false) }
    var isCustomForBreak by remember { mutableStateOf(false) }

    // --- NOTIFICATIONS & HAPTICS (Unchanged) ---
    var previousMembers by remember { mutableStateOf(liveState.members) }
    LaunchedEffect(liveState.members) {
        val joined = liveState.members.filter { new -> previousMembers.none { old -> old.userId == new.userId } }
        val left = previousMembers.filter { old -> liveState.members.none { new -> new.userId == old.userId } }
        joined.forEach { member -> if (member.userId != liveState.currentUserId) snackbarHostState.showSnackbar("🚀 ${member.displayName} joined") }
        left.forEach { member -> if (member.userId != liveState.currentUserId) snackbarHostState.showSnackbar("👋 ${member.displayName} left") }
        previousMembers = liveState.members
    }

    var previousTimerState by remember { mutableStateOf(liveState.timerState) }
    LaunchedEffect(liveState.timerState) {
        if (previousTimerState != liveState.timerState) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            previousTimerState = liveState.timerState
        }
    }

    var previousSeconds by remember { mutableStateOf(liveState.remainingSeconds) }
    LaunchedEffect(liveState.remainingSeconds) {
        if (previousSeconds > 0 && liveState.remainingSeconds == 0) {
            try {
                val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                RingtoneManager.getRingtone(context, uri)?.play()
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(800, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION") vibrator.vibrate(800)
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
        previousSeconds = liveState.remainingSeconds
    }

    BackHandler { showLeaveDialog = true }

    // --- UI LAYOUT ---
    Column(horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit){
                detectTapGestures(onTap = {focusManager.clearFocus()})
            }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            Text(liveState.roomName.ifEmpty { "Study Room" }, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            if (liveState.isHost) {
                IconButton(onClick = { showRenameDialog = true }) { Icon(Icons.Filled.Edit, "Edit Room Name", tint = MaterialTheme.colorScheme.primary) }
            }
        }

        Text("Code: $roomCode", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 8.dp))
        Text(if (liveState.isConnected) "Status: Connected (Live)" else "Status: Reconnecting...", color = if (liveState.isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(bottom = 16.dp))

        // TIMER CARD
        val minutes = liveState.remainingSeconds / 60
        val seconds = liveState.remainingSeconds % 60
        val timerText = String.format(java.util.Locale.US, "%02d:%02d", minutes, seconds)
        val isRunning = liveState.timerState == "running"
        val isBreak = liveState.mode == "break"
        val cardContainer = if (isBreak) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant
        val cardContent = if (isBreak) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = cardContainer, contentColor = cardContent),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                Text(if (isBreak) "☕ Break Time" else "📚 Study Time", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = cardContent)
                Spacer(modifier = Modifier.height(4.dp))
                Text(timerText, style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.SemiBold, color = cardContent)
                Spacer(modifier = Modifier.height(8.dp))

                if (liveState.isHost) {
                    Button(
                        onClick = { if (isRunning) onPauseTimer() else onStartTimer() },
                        modifier = Modifier.fillMaxWidth(0.8f),
                        colors = if (isBreak) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary) else ButtonDefaults.buttonColors()
                    ) { Text(if (isRunning) "Pause Timer" else "Start Timer") }

                    if (!isRunning) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Set Study Time:", style = MaterialTheme.typography.labelMedium, color = cardContent)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(selected = !isBreak && minutes == 25, onClick = { onChangeDuration(25, "work") }, label = { Text("25m") })
                            FilterChip(selected = !isBreak && minutes == 50, onClick = { onChangeDuration(50, "work") }, label = { Text("50m") })
                            FilterChip(selected = false, onClick = { isCustomForBreak = false; showCustomTimeDialog = true }, label = { Text("+ Custom") })
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Set Break Time:", style = MaterialTheme.typography.labelMedium, color = cardContent)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(selected = isBreak && minutes == 5, onClick = { onChangeDuration(5, "break") }, label = { Text("5m") })
                            FilterChip(selected = isBreak && minutes == 10, onClick = { onChangeDuration(10, "break") }, label = { Text("10m") })
                            FilterChip(selected = false, onClick = { isCustomForBreak = true; showCustomTimeDialog = true }, label = { Text("+ Custom") })
                        }
                    }
                } else {
                    Text("The host controls the timer.", color = cardContent, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // --- NEW: UPDATED MEMBERS LIST ---
        Text("Members (${liveState.members.size})", style = MaterialTheme.typography.titleMedium, modifier = Modifier.align(Alignment.Start).padding(bottom = 8.dp))

        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
            items(liveState.members) { member ->
                val isMe = member.userId == liveState.currentUserId

                ListItem(
                    headlineContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(if (isMe) "${member.displayName} (You)" else member.displayName, fontWeight = FontWeight.Bold)
                            if (member.userId == liveState.hostId) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("👑 Host", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    },
                    supportingContent = {


                        if (isMe) {
                            // CURRENT USER: Editable Text Field & Checkbox
                            var taskInput by remember { mutableStateOf(member.task) }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 4.dp).fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = taskInput,
                                    onValueChange = {
                                        taskInput = it
                                        // Instantly broadcast keystrokes to the room
                                        onUpdateTask(it, member.isTaskDone)
                                    },
                                    placeholder = { Text("What are you studying?") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    textStyle = MaterialTheme.typography.bodyMedium,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = {focusManager.clearFocus()})
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Checkbox(
                                    checked = member.isTaskDone,
                                    onCheckedChange = { isChecked ->
                                        onUpdateTask(taskInput, isChecked)
                                    }
                                )
                            }
                        } else {
                            // OTHER USERS: Read-only view
                            if (member.task.isNotBlank()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    Text(
                                        text = member.task,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (member.isTaskDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        textDecoration = if (member.isTaskDone) TextDecoration.LineThrough else null,
                                        modifier = Modifier.weight(1f, fill = false)
                                    )
                                    if (member.isTaskDone) {
                                        Text(" ✅", modifier = Modifier.padding(start = 4.dp))
                                    }
                                }
                            } else {
                                Text(
                                    text = "Selecting a task...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    fontStyle = FontStyle.Italic,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                )
                HorizontalDivider()
            }
        }

        OutlinedButton(onClick = { showLeaveDialog = true }, modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
            Text("Leave Room", color = MaterialTheme.colorScheme.error)
        }
    }

    // --- DIALOGS ---
    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false }, title = { Text("Leave Room?") },
            text = { Text("Are you sure you want to leave this study room? You will need the 6-digit code to rejoin.") },
            confirmButton = { TextButton(onClick = { showLeaveDialog = false; onLeaveRoom() }) { Text("Leave", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { showLeaveDialog = false }) { Text("Cancel") } }
        )
    }

    if (showRenameDialog) {
        var editNameInput by remember { mutableStateOf(liveState.roomName) }
        AlertDialog(
            onDismissRequest = { showRenameDialog = false }, title = { Text("Rename Room") },
            text = { OutlinedTextField(value = editNameInput, onValueChange = { editNameInput = it }, singleLine = true, modifier = Modifier.fillMaxWidth()) },
            confirmButton = { TextButton(onClick = { if (editNameInput.isNotBlank()) onRenameRoom(editNameInput); showRenameDialog = false }) { Text("Save") } },
            dismissButton = { TextButton(onClick = { showRenameDialog = false }) { Text("Cancel") } }
        )
    }

    if (showCustomTimeDialog) {
        var customMinutes by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCustomTimeDialog = false }, title = { Text(if (isCustomForBreak) "Custom Break Time" else "Custom Study Time") },
            text = { OutlinedTextField(value = customMinutes, onValueChange = { if (it.all { char -> char.isDigit() }) customMinutes = it }, label = { Text("Minutes") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth()) },
            confirmButton = { TextButton(onClick = { customMinutes.toIntOrNull()?.let { if (it in 1..999) onChangeDuration(it, if (isCustomForBreak) "break" else "work") }; showCustomTimeDialog = false }) { Text("Set") } },
            dismissButton = { TextButton(onClick = { showCustomTimeDialog = false }) { Text("Cancel") } }
        )
    }
}