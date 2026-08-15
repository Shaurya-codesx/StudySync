package com.example.studysyncandroid.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
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
    onLogout: () -> Unit,
    onNavigateToResetPassword: (String) -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profileState by viewModel.profileState.collectAsState()
    val otpSent by viewModel.otpSent.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    var newDisplayName by remember { mutableStateOf("") }

    LaunchedEffect(otpSent) {
        if (otpSent) {
            val email = (profileState as? ProfileUiState.Success)?.profile?.email ?: ""
            onNavigateToResetPassword(email)
            viewModel.resetOtpState()
        }
    }

    Scaffold(
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
                    CircularProgressIndicator(color = colorResource(id = R.color.deck_list_accent))
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

                    // Reset Password Button
                    Button(
                        onClick = { viewModel.sendResetOtp(email) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.deck_list_accent))
                    ) {
                        Icon(Icons.Default.LockReset, contentDescription = null, tint = colorResource(id = R.color.deck_list_bg))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Reset Password", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = colorResource(id = R.color.deck_list_bg))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Logout Button
                    OutlinedButton(
                        onClick = { showLogoutDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Log Out", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
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
    }
}
