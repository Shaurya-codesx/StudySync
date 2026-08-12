package com.example.studysyncandroid.ui.marketplace

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.studysyncandroid.R
import com.example.studysyncandroid.data.remote.dto.DeckSummaryResponse

@Composable
fun MarketplaceScreen(
    onBackClick: () -> Unit,
    viewModel: MarketplaceViewModel = hiltViewModel()
) {
    val pagingItems = viewModel.publicDecks.collectAsLazyPagingItems()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val bg = colorResource(id = R.color.deck_list_bg)
    val cardBg = colorResource(id = R.color.deck_list_card_bg)
    val textPrimary = colorResource(id = R.color.deck_list_text_primary)
    val textSecondary = colorResource(id = R.color.deck_list_text_secondary)
    val accent = colorResource(id = R.color.deck_list_accent)

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        containerColor = bg,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            
            // Header Section
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBackClick, modifier = Modifier.padding(end = 8.dp).offset(x = (-12).dp)) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = textPrimary)
                    }
                    Text(
                        text = "Marketplace", 
                        fontFamily = FontFamily.Serif, 
                        fontSize = 32.sp, 
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(imageVector = Icons.Default.Public, contentDescription = "Global", tint = textSecondary, modifier = Modifier.size(28.dp))
                }
                Text(
                    text = "Discover and clone amazing study decks shared by the community.",
                    fontSize = 16.sp,
                    color = textSecondary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )
            }
            
            Box(modifier = Modifier.fillMaxSize()) {
                if (pagingItems.loadState.refresh is LoadState.Loading) {
                    CircularProgressIndicator(color = accent, modifier = Modifier.align(Alignment.Center))
                } else if (pagingItems.loadState.refresh is LoadState.Error && pagingItems.itemCount == 0) {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Unable to load marketplace", fontSize = 18.sp, color = textPrimary, fontFamily = FontFamily.Serif)
                        Spacer(Modifier.height(8.dp))
                        Text("Please check your connection and try again.", fontSize = 14.sp, color = textSecondary, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = { pagingItems.retry() },
                            colors = ButtonDefaults.buttonColors(containerColor = accent)
                        ) {
                            Text("Retry", fontSize = 16.sp)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(pagingItems.itemCount) { index ->
                            val deck = pagingItems[index]
                            if (deck != null) {
                                MarketplaceDeckCard(deck = deck, onClone = { viewModel.cloneDeck(deck.id) })
                            }
                        }

                        pagingItems.apply {
                            when (loadState.append) {
                                is LoadState.Loading -> {
                                    item {
                                        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                            CircularProgressIndicator(color = accent, modifier = Modifier.size(32.dp))
                                        }
                                    }
                                }
                                is LoadState.Error -> {
                                    item {
                                        Column(
                                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text("Error loading more items", color = MaterialTheme.colorScheme.error)
                                            TextButton(onClick = { retry() }) { Text("Retry", color = accent) }
                                        }
                                    }
                                }
                                else -> {}
                            }
                        }
                    }
                    
                    if (pagingItems.itemCount == 0 && pagingItems.loadState.append.endOfPaginationReached) {
                        Column(
                            modifier = Modifier.align(Alignment.Center).padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(imageVector = Icons.Default.Public, contentDescription = "Empty", tint = textSecondary.copy(alpha = 0.5f), modifier = Modifier.size(64.dp).padding(bottom = 16.dp))
                            Text(
                                "No public decks available yet.",
                                fontSize = 18.sp,
                                color = textPrimary,
                                fontFamily = FontFamily.Serif
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Be the first to publish a deck!",
                                fontSize = 14.sp,
                                color = textSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MarketplaceDeckCard(
    deck: DeckSummaryResponse,
    onClone: () -> Unit
) {
    val cardBg = colorResource(id = R.color.deck_list_card_bg)
    val textPrimary = colorResource(id = R.color.deck_list_text_primary)
    val textSecondary = colorResource(id = R.color.deck_list_text_secondary)
    val dividerColor = colorResource(id = R.color.deck_list_divider)
    val borderColor = colorResource(id = R.color.deck_list_border)
    val accent = colorResource(id = R.color.deck_list_accent)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            // Left color bar
            Box(modifier = Modifier.fillMaxHeight().width(6.dp).background(dividerColor))
            
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 20.dp, bottom = 20.dp, end = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
                    Text(
                        text = deck.title, 
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = FontFamily.Serif,
                        color = textPrimary,
                        maxLines = 2
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "${deck.cardCount} cards",
                        fontSize = 14.sp,
                        color = textSecondary
                    )
                }
                
                Button(
                    onClick = onClone,
                    colors = ButtonDefaults.buttonColors(containerColor = accent),
                    shape = RoundedCornerShape(24.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(imageVector = Icons.Default.CloudDownload, contentDescription = "Clone", modifier = Modifier.size(18.dp), tint = colorResource(id = R.color.white))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clone", fontSize = 14.sp, color = colorResource(id = R.color.white))
                }
            }
        }
    }
}
