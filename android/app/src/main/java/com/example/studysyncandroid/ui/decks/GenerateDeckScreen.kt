package com.example.studysyncandroid.ui.decks

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.studysyncandroid.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenerateDeckScreen(
    onDeckGenerated: (deckId: String) -> Unit,
    onBackClick: () -> Unit,
    viewModel: GenerateDeckViewModel = hiltViewModel()
) {
    var noteText by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState) {
        val state = uiState
        if (state is GenerateDeckUiState.Success) {
            onDeckGenerated(state.deckId)
        }
    }

    val bg = colorResource(id = R.color.deck_list_bg)
    val cardBg = colorResource(id = R.color.deck_list_card_bg)
    val textPrimary = colorResource(id = R.color.deck_list_text_primary)
    val textSecondary = colorResource(id = R.color.deck_list_text_secondary)
    val borderColor = colorResource(id = R.color.deck_list_border)
    val accent = colorResource(id = R.color.deck_list_accent)

    Scaffold(
        containerColor = bg,
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = textPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = bg,
                    navigationIconContentColor = textPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            Text(
                "Paste Your Notes", 
                fontFamily = FontFamily.Serif,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = textPrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                "Our AI will magically turn your notes into a beautiful set of flashcards.", 
                fontSize = 16.sp,
                color = textSecondary,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Paper-like canvas for input
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                placeholder = { 
                    Text("Start typing or paste your study notes here...", color = textSecondary.copy(alpha = 0.7f)) 
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = cardBg,
                    unfocusedContainerColor = cardBg,
                    focusedBorderColor = textSecondary,
                    unfocusedBorderColor = borderColor,
                    cursorColor = textPrimary,
                    focusedTextColor = textPrimary,
                    unfocusedTextColor = textPrimary
                ),
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                )
            )

            val state = uiState
            if (state is GenerateDeckUiState.Error) {
                Text(
                    text = state.message, 
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Generate Button
            Button(
                onClick = { viewModel.generateDeck(noteText.trim()) },
                enabled = state !is GenerateDeckUiState.Loading && noteText.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(bottom = 24.dp), // Extra padding from bottom of screen
                shape = RoundedCornerShape(32.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accent,
                    disabledContainerColor = accent.copy(alpha = 0.5f),
                    contentColor = colorResource(id = R.color.white)
                )
            ) {
                if (state is GenerateDeckUiState.Loading) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = colorResource(id = R.color.white),
                            modifier = Modifier.size(24.dp), 
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Generating magic...", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                    }
                } else {
                    Text("Generate Deck", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}