package com.example.studysyncandroid.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.studysyncandroid.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    onSignupSuccess: (String) -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var displayName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val signupState by viewModel.signupState.collectAsStateWithLifecycle()

    val textPrimary = colorResource(id = R.color.deck_list_text_primary)
    val textSecondary = colorResource(id = R.color.deck_list_text_secondary)
    val cardBg = colorResource(id = R.color.deck_list_card_bg)
    val borderColor = colorResource(id = R.color.deck_list_border)
    val accent = colorResource(id = R.color.deck_list_accent)

    LaunchedEffect(signupState) {
        if (signupState is AuthUiState.RequiresVerification || signupState is AuthUiState.Success) {
            onSignupSuccess(email)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "StudySync", 
            fontFamily = FontFamily.Serif,
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = textPrimary
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Create an account.",
            fontSize = 18.sp,
            color = textSecondary,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        val textFieldColors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = accent,
            unfocusedBorderColor = borderColor,
            focusedContainerColor = cardBg,
            unfocusedContainerColor = cardBg,
            focusedTextColor = textPrimary,
            unfocusedTextColor = textPrimary,
            cursorColor = accent
        )

        OutlinedTextField(
            value = displayName,
            onValueChange = { displayName = it },
            label = { Text("Display Name", color = textSecondary) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = textFieldColors
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email", color = textSecondary) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = textFieldColors
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password", color = textSecondary) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = textFieldColors
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password", color = textSecondary) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = textFieldColors
        )
        
        Spacer(Modifier.height(8.dp))
        
        val passwordsMatch = password == confirmPassword
        val showMismatchError = password.isNotEmpty() && confirmPassword.isNotEmpty() && !passwordsMatch
        
        if (showMismatchError) {
            Text(
                text = "Passwords do not match", 
                color = MaterialTheme.colorScheme.error, 
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.Start).padding(start = 8.dp)
            )
        }
        
        Spacer(Modifier.height(16.dp))

        val state = signupState
        if (state is AuthUiState.Error) {
            Text(text = state.message, color = MaterialTheme.colorScheme.error, fontSize = 14.sp)
            Spacer(Modifier.height(16.dp))
        }

        Button(
            onClick = { viewModel.signup(email.trim(), password, displayName.trim()) },
            enabled = state !is AuthUiState.Loading &&
                    email.isNotBlank() && password.isNotBlank() && displayName.isNotBlank() &&
                    passwordsMatch,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = textPrimary,
                disabledContainerColor = textPrimary.copy(alpha = 0.5f)
            )
        ) {
            if (state is AuthUiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text("Sign Up", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = cardBg)
            }
        }
        
        Spacer(Modifier.height(24.dp))
        
        TextButton(onClick = onNavigateToLogin) {
            Text("Already have an account? Log in", color = textSecondary, fontSize = 16.sp)
        }
    }
}