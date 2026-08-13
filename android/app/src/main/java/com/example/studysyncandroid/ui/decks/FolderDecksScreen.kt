package com.example.studysyncandroid.ui.decks

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studysyncandroid.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.studysyncandroid.data.local.entities.DeckEntity
import kotlin.random.Random

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
                        .padding(start = 16.dp, end = 32.dp, top = 8.dp, bottom = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = textPrimary)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = folderWithDecks?.folder?.name ?: "Folder",
                        fontFamily = FontFamily.Serif,
                        fontSize = 30.sp,
                        color = textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (folderWithDecks == null || folderWithDecks.decks.isEmpty()) {
                    FolderEmptyState(textPrimary, textSecondary, accent, cardBg)
                } else {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 100.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalItemSpacing = 24.dp
                    ) {
                        items(folderWithDecks.decks, key = { "deck_${it.id}" }) { deck ->
                            StackedDeckCard(
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

        if (deckToMove != null) {
            FolderNordicDialog(
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
                                    Text("Remove from Folder (Uncategorized)", color = textSecondary)
                                }
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
            FolderNordicDialog(
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
    }
}

@Composable
private fun FolderEmptyState(
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
            Text("Folder is empty", fontFamily = FontFamily.Serif, fontSize = 24.sp, color = textPrimary)
            Spacer(Modifier.height(6.dp))
            Text(
                "You haven't moved any decks here yet",
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
private fun StackedDeckCard(
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
    val scale by animateFloatAsState(if (pressed) 0.95f else 1f, tween(120), label = "deckScale")

    // Use a fixed random seed based on deck id to keep the aspect ratio consistent
    val rand = remember(deck.id) { Random(deck.id.hashCode()) }
    val aspectRatio = remember(deck.id) { rand.nextFloat() * (0.85f - 0.70f) + 0.70f }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .aspectRatio(aspectRatio)
            .padding(8.dp) // Provide some padding for the stacked cards underneath
    ) {
        // Bottom stacked card (outjogged to the right and tilted)
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 12.dp, top = 6.dp)
                .rotate(4f),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, borderColor.copy(alpha = 0.5f)),
            colors = CardDefaults.cardColors(containerColor = surface.copy(alpha = 0.7f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) { Box(modifier = Modifier.fillMaxSize()) }

        // Middle stacked card (outjogged to the left and tilted)
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(end = 8.dp, top = 3.dp)
                .rotate(-3f),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, borderColor.copy(alpha = 0.8f)),
            colors = CardDefaults.cardColors(containerColor = surface.copy(alpha = 0.9f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) { Box(modifier = Modifier.fillMaxSize()) }

        // Front Card
        Card(
            modifier = Modifier
                .fillMaxSize()
                .combinedClickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick,
                    onLongClick = onLongClick
                ),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, borderColor),
            colors = CardDefaults.cardColors(containerColor = surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier.padding(16.dp).fillMaxSize()
                ) {
                    HorizontalDivider(color = dividerColor, thickness = 1.dp, modifier = Modifier.padding(bottom = 12.dp))
                    Text(
                        text = deck.title,
                        fontFamily = FontFamily.Serif,
                        fontSize = 18.sp,
                        color = textPrimary,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${deck.cardCount} cards",
                            fontSize = 12.sp,
                            color = textSecondary
                        )
                        if (deck.isPublic) {
                            Spacer(Modifier.width(6.dp))
                            Icon(
                                androidx.compose.material.icons.Icons.Default.Public,
                                contentDescription = "Published",
                                tint = textSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }


                // Options Menu
                Box(modifier = Modifier.align(Alignment.BottomEnd).padding(4.dp)) {
                    IconButton(
                        onClick = { showMenu = !showMenu },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = textSecondary,
                            modifier = Modifier.size(18.dp)
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
}

@Composable
private fun FolderNordicDialog(
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