package com.example.studysyncandroid.ui.decks

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.rotate
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.studysyncandroid.R
import com.example.studysyncandroid.data.local.entities.DeckEntity
import com.example.studysyncandroid.data.local.entities.FolderWithDecks
import com.example.studysyncandroid.ui.theme.NordicType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeckListScreen(
    onDeckClick: (deckId: String) -> Unit,
    onFolderClick: (folderId: String) -> Unit,
    onProfileClick: () -> Unit,
    deckListViewModel: DeckListViewModel = hiltViewModel(),
    folderViewModel: FolderViewModel = hiltViewModel()
) {
    val decks by deckListViewModel.decks.collectAsStateWithLifecycle()
    val isRefreshingDecks by deckListViewModel.isRefreshing.collectAsStateWithLifecycle()

    val folders by folderViewModel.foldersWithDecks.collectAsStateWithLifecycle()
    val isRefreshingFolders by folderViewModel.isRefreshing.collectAsStateWithLifecycle()

    val isRefreshing = isRefreshingDecks || isRefreshingFolders

    var deckToMove by remember { mutableStateOf<String?>(null) }
    var deckToDelete by remember { mutableStateOf<DeckEntity?>(null) }
    var folderToDelete by remember { mutableStateOf<FolderWithDecks?>(null) }

    // ---- Palette ----------------------------------------------------
    val bg = colorResource(id = R.color.deck_list_bg)
    val cardBg = colorResource(id = R.color.deck_list_card_bg)
    val textPrimary = colorResource(id = R.color.deck_list_text_primary)
    val textSecondary = colorResource(id = R.color.deck_list_text_secondary)
    val dividerColor = colorResource(id = R.color.deck_list_divider)
    val borderColor = colorResource(id = R.color.deck_list_border)
    val error = colorResource(id = R.color.deck_list_error)
    val accent = colorResource(id = R.color.deck_list_accent)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = bg
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .background(bg)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 32.dp, end = 32.dp, top = 8.dp, bottom = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Your decks",
                        fontFamily = FontFamily.Serif,
                        fontSize = 34.sp,
                        color = textPrimary
                    )
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Default.Person, contentDescription = "Profile", tint = textPrimary)
                    }
                }
                when {
                    isRefreshing && decks.isEmpty() && folders.isEmpty() -> {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = accent, strokeWidth = 2.dp)
                        }
                    }
                    decks.isEmpty() && folders.isEmpty() -> {
                        EmptyState(textPrimary = textPrimary, textSecondary = textSecondary, accent = accent, surface = cardBg)
                    }
                    else -> {
                        val uncategorizedDecks = decks.filter { it.folderId == null }
                        LazyColumn(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            contentPadding = PaddingValues(start = 32.dp, end = 32.dp, top = 8.dp, bottom = 100.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                        if (folders.isNotEmpty()) {
                            item {
                                Text(
                                    "FOLDERS",
                                    style = NordicType.sectionLabel,
                                    color = textSecondary,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                                )
                            }
                            items(folders, key = { "folder_${it.folder.id}" }) { folderWithDecks ->
                                FolderRow(
                                    folderWithDecks = folderWithDecks,
                                    onClick = { onFolderClick(folderWithDecks.folder.id) },
                                    onLongClick = { folderToDelete = folderWithDecks },
                                    surface = cardBg,
                                    borderColor = borderColor,
                                    dividerColor = dividerColor,
                                    textPrimary = textPrimary,
                                    textSecondary = textSecondary
                                )
                            }
                        }

                        if (uncategorizedDecks.isNotEmpty()) {
                            item {
                                Text(
                                    "DECKS",
                                    style = NordicType.sectionLabel,
                                    color = textSecondary,
                                    modifier = Modifier.padding(
                                        top = if (folders.isNotEmpty()) 16.dp else 8.dp, 
                                        bottom = 4.dp
                                    )
                                )
                            }
                            items(uncategorizedDecks, key = { "deck_${it.id}" }) { deck ->
                                DeckRow(
                                    deck = deck,
                                    onClick = { onDeckClick(deck.id) },
                                    onLongClick = { deckToDelete = deck },
                                    onMoveToFolder = { deckId -> deckToMove = deckId },
                                    onPublish = { deckId -> deckListViewModel.publishDeck(deckId) },
                                    onUnpublish = { deckId -> deckListViewModel.unpublishDeck(deckId) },
                                    surface = cardBg,
                                    borderColor = borderColor,
                                    dividerColor = dividerColor,
                                    textPrimary = textPrimary,
                                    textSecondary = textSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        if (deckToMove != null) {
            NordicDialog(
                onDismiss = { deckToMove = null },
                title = "Move to Folder",
                surface = cardBg,
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                body = {
                    LazyColumn {
                        item {
                            TextButton(
                                onClick = {
                                    deckListViewModel.moveDeckToFolder(deckToMove!!, null)
                                    deckToMove = null
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Text("Uncategorized", color = textSecondary)
                                }
                            }
                        }
                        items(folders, key = { it.folder.id }) { f ->
                            TextButton(
                                onClick = {
                                    deckListViewModel.moveDeckToFolder(deckToMove!!, f.folder.id)
                                    deckToMove = null
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Outlined.Folder,
                                        contentDescription = null,
                                        tint = accent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Text(f.folder.name, color = textPrimary)
                                }
                            }
                        }
                    }
                },
                confirmLabel = "Cancel",
                confirmColor = accent,
                onConfirm = { deckToMove = null }
            )
        }

        if (deckToDelete != null) {
            NordicDialog(
                onDismiss = { deckToDelete = null },
                title = "Delete Deck",
                surface = cardBg,
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                body = {
                    Text(
                        "Delete '${deckToDelete?.title}'? This permanently removes all ${deckToDelete?.cardCount} cards inside.",
                        color = textSecondary
                    )
                },
                confirmLabel = "Delete",
                confirmColor = error,
                onConfirm = {
                    deckToDelete?.id?.let { deckListViewModel.deleteDeck(it) }
                    deckToDelete = null
                },
                dismissLabel = "Cancel",
                onDismissClick = { deckToDelete = null }
            )
        }

        if (folderToDelete != null) {
            NordicDialog(
                onDismiss = { folderToDelete = null },
                title = "Delete Folder",
                surface = cardBg,
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                body = {
                    Text(
                        "Delete '${folderToDelete?.folder?.name}'? This permanently removes it and all ${folderToDelete?.decks?.size} decks inside.",
                        color = textSecondary
                    )
                },
                confirmLabel = "Delete",
                confirmColor = error,
                onConfirm = {
                    folderToDelete?.folder?.id?.let { folderViewModel.deleteFolder(it) }
                    folderToDelete = null
                },
                dismissLabel = "Cancel",
                onDismissClick = { folderToDelete = null }
            )
        }
    }
}
}

@Composable
private fun EmptyState(
    textPrimary: Color,
    textSecondary: Color,
    accent: Color,
    surface: Color
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                shape = CircleShape,
                color = surface,
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        Icons.Default.Style,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
            Text("No decks yet", fontFamily = FontFamily.Serif, fontSize = 24.sp, color = textPrimary)
            Spacer(Modifier.height(6.dp))
            Text(
                "Tap the + button to create\nyour first study deck",
                fontSize = 16.sp,
                color = textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 40.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FolderRow(
    folderWithDecks: FolderWithDecks,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    surface: Color,
    borderColor: Color,
    dividerColor: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.98f else 1f, tween(120), label = "folderScale")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .rotate(-1f)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp).fillMaxWidth()
        ) {
            HorizontalDivider(color = dividerColor, thickness = 1.dp, modifier = Modifier.padding(bottom = 12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Folder, contentDescription = null, tint = textSecondary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    folderWithDecks.folder.name,
                    fontFamily = FontFamily.Serif,
                    fontSize = 20.sp,
                    color = textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "${folderWithDecks.decks.size} deck${if (folderWithDecks.decks.size == 1) "" else "s"}",
                fontSize = 14.sp,
                color = textSecondary
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DeckRow(
    deck: DeckEntity,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMoveToFolder: (String) -> Unit,
    onPublish: (String) -> Unit,
    onUnpublish: (String) -> Unit,
    surface: Color,
    borderColor: Color,
    dividerColor: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    var showMenu by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.98f else 1f, tween(120), label = "deckScale")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .rotate(-1.5f)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(20.dp).fillMaxWidth()
            ) {
                HorizontalDivider(color = dividerColor, thickness = 1.dp, modifier = Modifier.padding(bottom = 12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        deck.title,
                        fontFamily = FontFamily.Serif,
                        fontSize = 20.sp,
                        color = textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (deck.isPublic) {
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            Icons.Default.Public,
                            contentDescription = "Published",
                            tint = textSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "${deck.cardCount} card${if (deck.cardCount == 1) "" else "s"}",
                    fontSize = 14.sp,
                    color = textSecondary
                )
            }

            Box(modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)) {
                IconButton(onClick = { showMenu = !showMenu }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier
                        .background(surface)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    DropdownMenuItem(
                        text = { Text("Move to Folder", color = textPrimary) },
                        onClick = {
                            showMenu = false
                            onMoveToFolder(deck.id)
                        }
                    )
                    if (deck.isPublic) {
                        DropdownMenuItem(
                            text = { Text("Make Private", color = textPrimary) },
                            onClick = {
                                showMenu = false
                                onUnpublish(deck.id)
                            }
                        )
                    } else {
                        DropdownMenuItem(
                            text = { Text("Publish to Marketplace", color = textPrimary) },
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

@Composable
private fun NordicDialog(
    onDismiss: () -> Unit,
    title: String,
    surface: Color,
    textPrimary: Color,
    textSecondary: Color,
    body: @Composable () -> Unit,
    confirmLabel: String,
    confirmColor: Color,
    onConfirm: () -> Unit,
    dismissLabel: String? = null,
    onDismissClick: (() -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = surface,
        shape = RoundedCornerShape(22.dp),
        title = { Text(title, fontSize = 20.sp, color = textPrimary) },
        text = { body() },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmLabel, color = confirmColor)
            }
        },
        dismissButton = if (dismissLabel != null && onDismissClick != null) {
            {
                TextButton(onClick = onDismissClick) {
                    Text(dismissLabel, color = textSecondary)
                }
            }
        } else null
    )
}