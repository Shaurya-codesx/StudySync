package com.example.studysyncandroid.ui.decks

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun GenerateDeckScreen(
    onDeckGenerated: (deckId: String) -> Unit,
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text("Paste Your Notes", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = noteText,
            onValueChange = { noteText = it },
            label = { Text("Notes") },
            placeholder = { Text("Paste or type your study notes here...") },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
        Spacer(Modifier.height(16.dp))

        val state = uiState
        if (state is GenerateDeckUiState.Error) {
            Text(text = state.message, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }

        Button(
            onClick = { viewModel.generateDeck(noteText.trim()) },
            enabled = state !is GenerateDeckUiState.Loading && noteText.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
        ) {
            if (state is GenerateDeckUiState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text("Generate Deck")
            }
        }
    }
}