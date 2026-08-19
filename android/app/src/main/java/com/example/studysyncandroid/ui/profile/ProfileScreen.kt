package com.example.studysyncandroid.ui.profile

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.studysyncandroid.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToResetPassword: (String) -> Unit,
    onNavigateToManagePublicDecks: () -> Unit,
    onNavigateToAboutDeveloper: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profileState by viewModel.profileState.collectAsState()
    val otpSent by viewModel.otpSent.collectAsState()
    val accountDeleted by viewModel.accountDeleted.collectAsState()
    
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showDeleteWarningDialog by remember { mutableStateOf(false) }
    var showDeletePasswordDialog by remember { mutableStateOf(false) }
    var newDisplayName by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }

    LaunchedEffect(accountDeleted) {
        if (accountDeleted) {
            onLogout()
        }
    }

    LaunchedEffect(otpSent) {
        if (otpSent) {
            val email = (profileState as? ProfileUiState.Success)?.profile?.email ?: ""
            onNavigateToResetPassword(email)
            viewModel.resetOtpState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = colorResource(id = R.color.deck_list_text_primary)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colorResource(id = R.color.deck_list_text_primary)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.deck_list_bg)
                )
            )
        },
        containerColor = colorResource(id = R.color.deck_list_bg)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            when (val state = profileState) {
                is ProfileUiState.Loading -> {
                    ProfileSkeleton()
                }
                is ProfileUiState.Success -> {
                    val displayName = state.profile.displayName ?: "User"
                    val email = state.profile.email

                    // Profile Icon/Avatar
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(50.dp))
                            .background(colorResource(id = R.color.deck_list_card_bg)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile Icon",
                            tint = colorResource(id = R.color.deck_list_text_primary),
                            modifier = Modifier.size(50.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Hi, $displayName!",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorResource(id = R.color.deck_list_text_primary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                newDisplayName = displayName
                                showEditNameDialog = true
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Edit,
                                contentDescription = "Edit Name",
                                tint = colorResource(id = R.color.deck_list_accent)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = email,
                        fontSize = 16.sp,
                        color = colorResource(id = R.color.deck_list_text_secondary)
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    // Manage Public Decks Button
                    Button(
                        onClick = onNavigateToManagePublicDecks,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.deck_list_card_bg)),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Icon(androidx.compose.material.icons.Icons.Default.Visibility, contentDescription = null, tint = colorResource(id = R.color.deck_list_accent))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Manage Public Decks", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = colorResource(id = R.color.deck_list_text_primary))
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = colorResource(id = R.color.deck_list_text_secondary))
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // Reset Password Button
                    Button(
                        onClick = { viewModel.sendResetOtp(email) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.deck_list_card_bg)),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Icon(Icons.Default.LockReset, contentDescription = null, tint = colorResource(id = R.color.deck_list_accent))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Reset Password", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = colorResource(id = R.color.deck_list_text_primary))
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = colorResource(id = R.color.deck_list_text_secondary))
                    }

                    Spacer(modifier = Modifier.height(48.dp))

                    // Log Out and Delete Account Side by Side
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showLogoutDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Log Out", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error)
                        }

                        OutlinedButton(
                            onClick = { showDeleteWarningDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                        ) {
                            Icon(androidx.compose.material.icons.Icons.Default.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Delete", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // About the Developer Button
                    Button(
                        onClick = onNavigateToAboutDeveloper,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.deck_list_card_bg)),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Icon(androidx.compose.material.icons.Icons.Default.Info, contentDescription = null, tint = colorResource(id = R.color.deck_list_accent))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("About the Developer", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = colorResource(id = R.color.deck_list_text_primary))
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(androidx.compose.material.icons.Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = colorResource(id = R.color.deck_list_text_secondary))
                    }
                }
                is ProfileUiState.Error -> {
                    Text(
                        text = "Error: ${state.message}",
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.fetchProfile() },
                        colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.deck_list_accent))
                    ) {
                        Text("Retry")
                    }
                }
            }
        }

        // Logout Confirmation Dialog
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text("Log Out") },
                text = { Text("Are you sure you want to log out of your account?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showLogoutDialog = false
                            viewModel.logout(onComplete = onLogout)
                        }
                    ) {
                        Text("Log Out", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("Cancel", color = colorResource(id = R.color.deck_list_text_primary))
                    }
                },
                containerColor = colorResource(id = R.color.deck_list_card_bg),
                titleContentColor = colorResource(id = R.color.deck_list_text_primary),
                textContentColor = colorResource(id = R.color.deck_list_text_secondary)
            )
        }

        if (showEditNameDialog) {
            AlertDialog(
                onDismissRequest = { showEditNameDialog = false },
                title = { Text("Edit Display Name") },
                text = {
                    OutlinedTextField(
                        value = newDisplayName,
                        onValueChange = { newDisplayName = it },
                        label = { Text("Display Name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (newDisplayName.isNotBlank()) {
                                viewModel.updateDisplayName(newDisplayName)
                                showEditNameDialog = false
                            }
                        }
                    ) {
                        Text("Save", color = colorResource(id = R.color.deck_list_accent))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditNameDialog = false }) {
                        Text("Cancel", color = colorResource(id = R.color.deck_list_text_primary))
                    }
                },
                containerColor = colorResource(id = R.color.deck_list_card_bg),
                titleContentColor = colorResource(id = R.color.deck_list_text_primary)
            )
        }

        // Delete Account Warning Dialog
        if (showDeleteWarningDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteWarningDialog = false },
                title = { Text("Delete Account") },
                text = { Text("This action will permanently erase all data of your account and cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteWarningDialog = false
                            showDeletePasswordDialog = true
                        }
                    ) {
                        Text("Continue", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteWarningDialog = false }) {
                        Text("Cancel", color = colorResource(id = R.color.deck_list_text_primary))
                    }
                },
                containerColor = colorResource(id = R.color.deck_list_card_bg),
                titleContentColor = colorResource(id = R.color.deck_list_text_primary),
                textContentColor = colorResource(id = R.color.deck_list_text_secondary)
            )
        }

        // Delete Account Password Dialog
        if (showDeletePasswordDialog) {
            AlertDialog(
                onDismissRequest = { 
                    showDeletePasswordDialog = false 
                    passwordInput = ""
                },
                title = { Text("Verify Password") },
                text = {
                    Column {
                        Text("Enter your password to verify account deletion.", color = colorResource(id = R.color.deck_list_text_secondary))
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text("Password") },
                            singleLine = true,
                            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (passwordInput.isNotBlank()) {
                                viewModel.deleteAccount(passwordInput)
                                showDeletePasswordDialog = false
                                passwordInput = ""
                            }
                        }
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { 
                        showDeletePasswordDialog = false 
                        passwordInput = ""
                    }) {
                        Text("Cancel", color = colorResource(id = R.color.deck_list_text_primary))
                    }
                },
                containerColor = colorResource(id = R.color.deck_list_card_bg),
                titleContentColor = colorResource(id = R.color.deck_list_text_primary)
            )
        }
    }
}
@Composable
fun ProfileSkeleton() {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_alpha"
    )
    val shimmerColor = colorResource(id = R.color.deck_list_card_bg).copy(alpha = alpha)

    Box(
        modifier = Modifier
            .size(100.dp)
            .clip(RoundedCornerShape(50.dp))
            .background(shimmerColor)
    )

    Spacer(modifier = Modifier.height(24.dp))

    Box(
        modifier = Modifier
            .width(200.dp)
            .height(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(shimmerColor)
    )
    
    Spacer(modifier = Modifier.height(8.dp))

    Box(
        modifier = Modifier
            .width(150.dp)
            .height(20.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(shimmerColor)
    )

    Spacer(modifier = Modifier.height(48.dp))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(shimmerColor)
    )
    
    Spacer(modifier = Modifier.height(16.dp))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(shimmerColor)
    )

    Spacer(modifier = Modifier.height(48.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(shimmerColor)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(shimmerColor)
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(shimmerColor)
    )
}
