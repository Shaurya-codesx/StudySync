package com.example.studysyncandroid.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.VisibilityOff
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
import com.example.studysyncandroid.data.local.entities.DeckEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagePublicDecksScreen(
    onNavigateBack: () -> Unit,
    viewModel: ManagePublicDecksViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Public Decks", 
                        color = colorResource(id = R.color.deck_list_text_primary),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.offset(y = (-4).dp)
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colorResource(id = R.color.deck_list_text_primary)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorResource(id = R.color.deck_list_bg)
                )
            )
        },
        containerColor = colorResource(id = R.color.deck_list_bg)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is ManageDecksUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = colorResource(id = R.color.deck_list_accent)
                    )
                }
                is ManageDecksUiState.Success -> {
                    if (state.decks.isEmpty()) {
                        Text(
                            text = "You don't have any public decks.",
                            color = colorResource(id = R.color.deck_list_text_secondary),
                            fontSize = 16.sp,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            items(state.decks) { deck ->
                                PublicDeckItem(
                                    deck = deck,
                                    onUnpublish = { viewModel.unpublishDeck(deck.id) }
                                )
                            }
                        }
                    }
                }
                is ManageDecksUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Button(
                            onClick = { viewModel.refresh() },
                            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.deck_list_accent))
                        ) {
                            Text("Retry")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PublicDeckItem(
    deck: DeckEntity,
    onUnpublish: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colorResource(id = R.color.deck_list_card_bg))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = deck.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(id = R.color.deck_list_text_primary)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${deck.cardCount} Cards",
                fontSize = 14.sp,
                color = colorResource(id = R.color.deck_list_text_secondary)
            )
        }

        Box {
            IconButton(onClick = { expanded = true }) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "Options",
                    tint = colorResource(id = R.color.deck_list_text_primary)
                )
            }
            
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(colorResource(id = R.color.deck_list_card_bg))
            ) {
                DropdownMenuItem(
                    text = { Text("Make Private", color = colorResource(id = R.color.deck_list_text_primary)) },
                    onClick = {
                        expanded = false
                        onUnpublish()
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = colorResource(id = R.color.deck_list_text_primary)
                        )
                    }
                )
            }
        }
    }
}
