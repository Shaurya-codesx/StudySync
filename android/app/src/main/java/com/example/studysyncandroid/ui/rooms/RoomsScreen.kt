package com.example.studysyncandroid.ui.rooms

import android.content.Context
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.studysyncandroid.R
import com.example.studysyncandroid.data.remote.RoomLiveState

@Composable
fun RoomsScreen(
    viewModel: RoomViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val liveState by viewModel.liveState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent // Let NavGraph handle the background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues), // Removed the 16.dp padding
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

    val bg = colorResource(id = R.color.deck_list_bg)
    val textPrimary = colorResource(id = R.color.deck_list_text_primary)
    val accent = colorResource(id = R.color.deck_list_accent)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(24.dp).fillMaxWidth()
    ) {
            Text(
                "Study Rooms",
                fontFamily = FontFamily.Serif,
                fontSize = 28.sp,
                color = textPrimary,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            
            OutlinedTextField(
                value = roomName, 
                onValueChange = { roomName = it }, 
                label = { Text("New Room Name") }, 
                singleLine = true, 
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { onCreateRoom(roomName) }, 
                enabled = !isLoading && roomName.isNotBlank(), 
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accent)
            ) { Text("Create New Room", fontSize = 16.sp) }
            
            Text("— OR —", style = MaterialTheme.typography.labelLarge, color = textPrimary, modifier = Modifier.padding(vertical = 24.dp))
            
            OutlinedTextField(
                value = joinCode, 
                onValueChange = { joinCode = it.uppercase() }, 
                label = { Text("Enter 6-Digit Code") }, 
                singleLine = true, 
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { onJoinRoom(joinCode) }, 
                enabled = !isLoading && joinCode.isNotBlank(), 
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accent)
            ) { Text("Join Room", fontSize = 16.sp) }

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
    onUpdateTask: (String, Boolean) -> Unit
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val focusManager = LocalFocusManager.current

    var showRenameDialog by remember { mutableStateOf(false) }
    var showLeaveDialog by remember { mutableStateOf(false) }
    var showCustomTimeDialog by remember { mutableStateOf(false) }
    var isCustomForBreak by remember { mutableStateOf(false) }

    val bg = colorResource(id = R.color.deck_list_bg)
    val textPrimary = colorResource(id = R.color.deck_list_text_primary)
    val textSecondary = colorResource(id = R.color.deck_list_text_secondary)
    val dividerColor = colorResource(id = R.color.deck_list_divider)
    val borderColor = colorResource(id = R.color.deck_list_border)
    val accent = colorResource(id = R.color.deck_list_accent)

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

    // Main Column instead of Card
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit){ detectTapGestures(onTap = {focusManager.clearFocus()}) }
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // Header
            Row(
                verticalAlignment = Alignment.CenterVertically, 
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
            ) {
                Text(
                    text = liveState.roomName.ifEmpty { "Study room" }, 
                    fontFamily = FontFamily.Serif, 
                    fontSize = 28.sp, 
                    color = textPrimary,
                    modifier = Modifier.weight(1f)
                )
                if (liveState.isHost) {
                    IconButton(onClick = { showRenameDialog = true }, modifier = Modifier.size(32.dp)) { 
                        Icon(Icons.Filled.Edit, "Edit Room Name", tint = textSecondary, modifier = Modifier.size(20.dp)) 
                    }
                }
            }
            Text("Code · $roomCode", fontSize = 14.sp, color = textSecondary, modifier = Modifier.padding(bottom = 24.dp))
            
            if (!liveState.isConnected) {
                Text("Status: Reconnecting...", color = MaterialTheme.colorScheme.error, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
            }

            // TIMER COMPONENT
            val minutes = liveState.remainingSeconds / 60
            val seconds = liveState.remainingSeconds % 60
            val timerText = String.format(java.util.Locale.US, "%02d:%02d", minutes, seconds)
            val isRunning = liveState.timerState == "running"
            val isBreak = liveState.mode == "break"

            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = bg),
                border = BorderStroke(1.dp, borderColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally, 
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    HorizontalDivider(color = dividerColor, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                    
                    Text(
                        text = if (isBreak) "Break Time" else "Study Time",
                        fontSize = 12.sp,
                        color = textSecondary,
                        modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
                    )

                    Text(
                        text = timerText, 
                        fontFamily = FontFamily.Serif, 
                        fontSize = 64.sp, 
                        color = textPrimary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    if (liveState.isHost) {
                        if (!isRunning) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(bottom = 24.dp)) {
                                if (!isBreak) {
                                    OutlinedChip("-5 min", onClick = { if (minutes > 5) onChangeDuration(minutes - 5, "work") }, borderColor)
                                    OutlinedChip("25 m", onClick = { onChangeDuration(25, "work") }, borderColor)
                                    OutlinedChip("50 m", onClick = { onChangeDuration(50, "work") }, borderColor)
                                    OutlinedChip("+5 min", onClick = { onChangeDuration(minutes + 5, "work") }, borderColor)
                                } else {
                                    OutlinedChip("-2 min", onClick = { if (minutes > 2) onChangeDuration(minutes - 2, "break") }, borderColor)
                                    OutlinedChip("5 m", onClick = { onChangeDuration(5, "break") }, borderColor)
                                    OutlinedChip("10 m", onClick = { onChangeDuration(10, "break") }, borderColor)
                                    OutlinedChip("+2 min", onClick = { onChangeDuration(minutes + 2, "break") }, borderColor)
                                }
                            }
                            
                            Row(modifier = Modifier.padding(bottom = 16.dp)) {
                                TextButton(onClick = { isCustomForBreak = false; showCustomTimeDialog = true }) { Text("Custom Study", color = textSecondary) }
                                TextButton(onClick = { isCustomForBreak = true; showCustomTimeDialog = true }) { Text("Custom Break", color = textSecondary) }
                            }
                        }
                        
                        Button(
                            onClick = { if (isRunning) onPauseTimer() else onStartTimer() },
                            modifier = Modifier.fillMaxWidth(0.8f).height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = accent),
                            shape = RoundedCornerShape(24.dp)
                        ) { Text(if (isRunning) "Pause timer" else "Start timer", fontSize = 16.sp) }
                    } else {
                        Text("The host controls the timer.", color = textSecondary, fontSize = 14.sp, modifier = Modifier.padding(bottom = 16.dp))
                    }
                }
            }

            // MEMBERS LIST
            Text("${liveState.members.size} members", fontSize = 14.sp, color = textSecondary, modifier = Modifier.padding(bottom = 16.dp))

            LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                items(liveState.members) { member ->
                    val isMe = member.userId == liveState.currentUserId
                    val initialColor = getAvatarColor(member.userId)
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Avatar
                        Box(
                            modifier = Modifier.size(40.dp).clip(CircleShape).background(initialColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(getInitials(member.displayName), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            // Name Row
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = if (isMe) "${member.displayName} (You)" else member.displayName, 
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = textPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                if (member.userId == liveState.hostId) {
                                    Box(
                                        modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(bg).padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text("Host", fontSize = 12.sp, color = dividerColor)
                                    }
                                }
                            }
                            
                            // Task Input/View
                            if (isMe) {
                                var taskInput by remember { mutableStateOf(member.task) }
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp).fillMaxWidth()) {
                                    OutlinedTextField(
                                        value = taskInput,
                                        onValueChange = {
                                            taskInput = it
                                            onUpdateTask(it, member.isTaskDone)
                                        },
                                        placeholder = { Text("What are you studying?") },
                                        modifier = Modifier.weight(1f).height(50.dp),
                                        singleLine = true,
                                        textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                        keyboardActions = KeyboardActions(onDone = {focusManager.clearFocus()}),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            unfocusedBorderColor = borderColor,
                                            focusedBorderColor = textSecondary,
                                            cursorColor = textPrimary
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Checkbox(
                                        checked = member.isTaskDone,
                                        onCheckedChange = { isChecked -> onUpdateTask(taskInput, isChecked) },
                                        colors = CheckboxDefaults.colors(checkedColor = accent)
                                    )
                                }
                            } else {
                                if (member.task.isNotBlank()) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                                        Text(
                                            text = member.task,
                                            fontSize = 14.sp,
                                            color = if (member.isTaskDone) textSecondary else textPrimary,
                                            textDecoration = if (member.isTaskDone) TextDecoration.LineThrough else null,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        if (member.isTaskDone) {
                                            Text(" ✅", modifier = Modifier.padding(start = 4.dp))
                                        }
                                    }
                                } else {
                                    Text(
                                        text = "Working quietly...",
                                        fontSize = 14.sp,
                                        color = textSecondary,
                                        fontStyle = FontStyle.Italic,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                    HorizontalDivider(color = borderColor.copy(alpha = 0.5f))
                }
            }

            TextButton(onClick = { showLeaveDialog = true }, modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 16.dp)) {
                Text("Leave Room", color = MaterialTheme.colorScheme.error)
            }
        }

    // --- DIALOGS ---
    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false }, 
            title = { Text("Leave Room?", color = textPrimary) },
            containerColor = bg,
            text = { Text("Are you sure you want to leave this study room? You will need the 6-digit code to rejoin.", color = textSecondary) },
            confirmButton = { TextButton(onClick = { showLeaveDialog = false; onLeaveRoom() }) { Text("Leave", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { showLeaveDialog = false }) { Text("Cancel", color = textSecondary) } }
        )
    }

    if (showRenameDialog) {
        var editNameInput by remember { mutableStateOf(liveState.roomName) }
        AlertDialog(
            onDismissRequest = { showRenameDialog = false }, 
            title = { Text("Rename Room", color = textPrimary) },
            containerColor = bg,
            text = { OutlinedTextField(value = editNameInput, onValueChange = { editNameInput = it }, singleLine = true, modifier = Modifier.fillMaxWidth()) },
            confirmButton = { TextButton(onClick = { if (editNameInput.isNotBlank()) onRenameRoom(editNameInput); showRenameDialog = false }) { Text("Save", color = accent) } },
            dismissButton = { TextButton(onClick = { showRenameDialog = false }) { Text("Cancel", color = textSecondary) } }
        )
    }

    if (showCustomTimeDialog) {
        var customMinutes by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCustomTimeDialog = false }, 
            title = { Text(if (isCustomForBreak) "Custom Break Time" else "Custom Study Time", color = textPrimary) },
            containerColor = bg,
            text = { OutlinedTextField(value = customMinutes, onValueChange = { if (it.all { char -> char.isDigit() }) customMinutes = it }, label = { Text("Minutes") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth()) },
            confirmButton = { TextButton(onClick = { customMinutes.toIntOrNull()?.let { if (it in 1..999) onChangeDuration(it, if (isCustomForBreak) "break" else "work") }; showCustomTimeDialog = false }) { Text("Set", color = accent) } },
            dismissButton = { TextButton(onClick = { showCustomTimeDialog = false }) { Text("Cancel", color = textSecondary) } }
        )
    }
}

@Composable
fun OutlinedChip(label: String, onClick: () -> Unit, borderColor: Color) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, borderColor),
        color = Color.Transparent,
        modifier = Modifier.padding(2.dp)
    ) {
        TextButton(onClick = onClick, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
            Text(label, color = colorResource(id = R.color.deck_list_text_secondary), fontSize = 12.sp)
        }
    }
}

fun getAvatarColor(userId: String): Color {
    val hash = userId.hashCode()
    val colors = listOf(Color(0xFFE5AFA5), Color(0xFFC3C7A3), Color(0xFFC5C2AE), Color(0xFFB1C5D4), Color(0xFFE0C4B8))
    return colors[kotlin.math.abs(hash) % colors.size]
}

fun getInitials(name: String): String {
    return name.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercase() }.joinToString("")
}