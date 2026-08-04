package com.example.studysyncandroid.ui.decks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.studysyncandroid.data.local.entities.DeckEntity
import com.example.studysyncandroid.ui.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckListScreen(
    onDeckClick: (deckId: String) -> Unit,
    onGenerateDeckClick: () -> Unit,
    onRoomsClick: () -> Unit,
    onLogout: () -> Unit,
    deckListViewModel: DeckListViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val decks by deckListViewModel.decks.collectAsStateWithLifecycle()
    val isRefreshing by deckListViewModel.isRefreshing.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Decks") },
                actions = {
                    TextButton(onClick = { deckListViewModel.refresh() }) {
                        Text("Refresh")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onGenerateDeckClick) {
                Text("+", style = MaterialTheme.typography.headlineSmall)
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Box(modifier = Modifier.weight(1f)) {
                when {
                    isRefreshing && decks.isEmpty() -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    decks.isEmpty() -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No decks yet — tap + to generate one from your notes")
                        }
                    }
                    else -> {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(decks, key = { it.id }) { deck ->
                                DeckRow(deck = deck, onClick = { onDeckClick(deck.id) })
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(onClick = onRoomsClick) {
                    Text("Go to Rooms")
                }
                OutlinedButton(onClick = { authViewModel.logout(onComplete = onLogout) }) {
                    Text("Log Out")
                }
            }
        }
    }
}

@Composable
private fun DeckRow(deck: DeckEntity, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(deck.title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                "${deck.cardCount} cards",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}