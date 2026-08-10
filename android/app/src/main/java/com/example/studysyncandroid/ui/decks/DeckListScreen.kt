package com.example.studysyncandroid.ui.decks

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.example.studysyncandroid.data.local.entities.FolderWithDecks
import com.example.studysyncandroid.ui.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckListScreen(
    onDeckClick: (deckId: String) -> Unit,
    onFolderClick: (folderId: String) -> Unit,
    onGenerateDeckClick: () -> Unit,
    onRoomsClick: () -> Unit,
    onMarketplaceClick: () -> Unit,
    onAnalyticsClick: () -> Unit,
    onLogout: () -> Unit,
    deckListViewModel: DeckListViewModel = hiltViewModel(),
    folderViewModel: FolderViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val decks by deckListViewModel.decks.collectAsStateWithLifecycle()
    val isRefreshingDecks by deckListViewModel.isRefreshing.collectAsStateWithLifecycle()
    
    val folders by folderViewModel.foldersWithDecks.collectAsStateWithLifecycle()
    val isRefreshingFolders by folderViewModel.isRefreshing.collectAsStateWithLifecycle()

    val isRefreshing = isRefreshingDecks || isRefreshingFolders

    var showFabMenu by remember { mutableStateOf(false) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    
    var deckToMove by remember { mutableStateOf<String?>(null) }
    var deckToDelete by remember { mutableStateOf<DeckEntity?>(null) }
    var folderToDelete by remember { mutableStateOf<FolderWithDecks?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Decks") },
                actions = {
                    TextButton(onClick = { 
                        deckListViewModel.refresh() 
                        folderViewModel.refresh()
                    }) {
                        Text("Refresh")
                    }
                }
            )
        },
        floatingActionButton = {
            Box {
                FloatingActionButton(onClick = { showFabMenu = !showFabMenu }) {
                    Text("+", style = MaterialTheme.typography.headlineSmall)
                }
                DropdownMenu(
                    expanded = showFabMenu,
                    onDismissRequest = { showFabMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Create Deck") },
                        onClick = {
                            showFabMenu = false
                            onGenerateDeckClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Create Folder") },
                        onClick = {
                            showFabMenu = false
                            showCreateFolderDialog = true
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                when {
                    isRefreshing && decks.isEmpty() && folders.isEmpty() -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    decks.isEmpty() && folders.isEmpty() -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No decks or folders yet — tap + to start")
                        }
                    }
                    else -> {
                        val uncategorizedDecks = decks.filter { it.folderId == null }
                        LazyVerticalStaggeredGrid(
                            columns = StaggeredGridCells.Fixed(2),
                            modifier = Modifier.fillMaxSize(),
                            verticalItemSpacing = 16.dp,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            if (folders.isNotEmpty()) {
                                item(span = StaggeredGridItemSpan.FullLine) {
                                    Text(
                                        "Folders",
                                        style = MaterialTheme.typography.titleSmall,
                                        modifier = Modifier.padding(bottom = 8.dp),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                items(folders, key = { it.folder.id }) { folderWithDecks ->
                                    FolderCard(
                                        folderWithDecks = folderWithDecks, 
                                        onClick = { onFolderClick(folderWithDecks.folder.id) },
                                        onLongClick = { folderToDelete = folderWithDecks }
                                    )
                                }
                            }
                            
                            if (uncategorizedDecks.isNotEmpty()) {
                                item(span = StaggeredGridItemSpan.FullLine) {
                                    Text(
                                        "Uncategorized Decks",
                                        style = MaterialTheme.typography.titleSmall,
                                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                items(uncategorizedDecks, key = { it.id }) { deck ->
                                    DeckCard(
                                        deck = deck, 
                                        onClick = { onDeckClick(deck.id) },
                                        onLongClick = { deckToDelete = deck },
                                        onMoveToFolder = { deckId -> deckToMove = deckId },
                                        onPublish = { deckId -> deckListViewModel.publishDeck(deckId) },
                                        onUnpublish = { deckId -> deckListViewModel.unpublishDeck(deckId) }
                                    )
                                }
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
                    Text("Rooms")
                }
                OutlinedButton(onClick = onMarketplaceClick) {
                    Text("Marketplace")
                }
                OutlinedButton(onClick = onAnalyticsClick) {
                    Text("Analytics")
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(onClick = { authViewModel.logout(onComplete = onLogout) }) {
                    Text("Log Out")
                }
            }
        }
        
        if (showCreateFolderDialog) {
            var folderName by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showCreateFolderDialog = false },
                title = { Text("Create Folder") },
                text = {
                    OutlinedTextField(
                        value = folderName,
                        onValueChange = { folderName = it },
                        label = { Text("Folder Name") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (folderName.isNotBlank()) {
                                folderViewModel.createFolder(folderName)
                                showCreateFolderDialog = false
                            }
                        }
                    ) {
                        Text("Create")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateFolderDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
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
                        items(folders, key = { it.folder.id }) { folderWithDecks ->
                            TextButton(
                                onClick = {
                                    deckListViewModel.moveDeckToFolder(deckToMove!!, folderWithDecks.folder.id)
                                    deckToMove = null
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("📁 ${folderWithDecks.folder.name}")
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

        if (folderToDelete != null) {
            AlertDialog(
                onDismissRequest = { folderToDelete = null },
                title = { Text("Delete Folder") },
                text = { Text("Delete '${folderToDelete?.folder?.name}'? This will permanently delete this folder and all ${folderToDelete?.decks?.size} decks inside it.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            folderToDelete?.folder?.id?.let { folderViewModel.deleteFolder(it) }
                            folderToDelete = null
                        }
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { folderToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FolderCard(
    folderWithDecks: FolderWithDecks,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("📁", style = MaterialTheme.typography.displaySmall)
            Spacer(Modifier.height(8.dp))
            Text(
                folderWithDecks.folder.name, 
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "${folderWithDecks.decks.size} decks",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DeckCard(
    deck: DeckEntity, 
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMoveToFolder: (String) -> Unit,
    onPublish: (String) -> Unit,
    onUnpublish: (String) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.padding(16.dp).fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(deck.title, style = MaterialTheme.typography.titleMedium, maxLines = 2)
                Spacer(Modifier.height(8.dp))
                Text(
                    "${deck.cardCount} cards",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Box(modifier = Modifier.align(Alignment.TopEnd)) {
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
                    if (deck.isPublic) {
                        DropdownMenuItem(
                            text = { Text("Make Deck Private") },
                            onClick = {
                                showMenu = false
                                onUnpublish(deck.id)
                            }
                        )
                    } else {
                        DropdownMenuItem(
                            text = { Text("Publish to Marketplace") },
                            onClick = {
                                showMenu = false
                                onPublish(deck.id)
                            }
                        )
                    }
                }
            }
        }
    }
}