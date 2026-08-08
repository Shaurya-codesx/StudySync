package com.example.studysyncandroid.ui.decks

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.studysyncandroid.data.local.entities.DeckEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderDecksScreen(
    folderId: String,
    onBack: () -> Unit,
    onDeckClick: (String) -> Unit,
    folderViewModel: FolderViewModel = hiltViewModel(),
    deckListViewModel: DeckListViewModel = hiltViewModel()
) {
    val foldersWithDecks by folderViewModel.foldersWithDecks.collectAsStateWithLifecycle()
    val folderWithDecks = foldersWithDecks.find { it.folder.id == folderId }
    
    var deckToMove by remember { mutableStateOf<String?>(null) }
    var deckToDelete by remember { mutableStateOf<DeckEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(folderWithDecks?.folder?.name ?: "Folder") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if (folderWithDecks == null || folderWithDecks.decks.isEmpty()) {
                Text(
                    "No decks in this folder",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(folderWithDecks.decks, key = { it.id }) { deck ->
                        FolderDeckRow(
                            deck = deck, 
                            onClick = { onDeckClick(deck.id) },
                            onLongClick = { deckToDelete = deck },
                            onMoveToFolder = { deckId -> deckToMove = deckId }
                        )
                    }
                }
            }
        }
        
        if (deckToMove != null) {
            AlertDialog(
                onDismissRequest = { deckToMove = null },
                title = { Text("Move to Folder") },
                text = {
                    LazyColumn {
                        item {
                            TextButton(
                                onClick = {
                                    deckListViewModel.moveDeckToFolder(deckToMove!!, null)
                                    deckToMove = null
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Remove from Folder (Uncategorized)")
                            }
                        }
                        items(foldersWithDecks, key = { it.folder.id }) { f ->
                            TextButton(
                                onClick = {
                                    deckListViewModel.moveDeckToFolder(deckToMove!!, f.folder.id)
                                    deckToMove = null
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("📁 ${f.folder.name}")
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { deckToMove = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
        
        if (deckToDelete != null) {
            AlertDialog(
                onDismissRequest = { deckToDelete = null },
                title = { Text("Delete Deck") },
                text = { Text("Are you sure you want to delete '${deckToDelete?.title}'? This will permanently delete all ${deckToDelete?.cardCount} cards inside.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            deckToDelete?.id?.let { deckListViewModel.deleteDeck(it) }
                            deckToDelete = null
                        }
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { deckToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FolderDeckRow(
    deck: DeckEntity, 
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMoveToFolder: (String) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(deck.title, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    "${deck.cardCount} cards",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                IconButton(onClick = { showMenu = !showMenu }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Options")
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Move to Folder") },
                        onClick = {
                            showMenu = false
                            onMoveToFolder(deck.id)
                        }
                    )
                }
            }
        }
    }
}
