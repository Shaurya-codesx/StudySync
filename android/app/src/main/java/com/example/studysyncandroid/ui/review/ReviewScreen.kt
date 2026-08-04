package com.example.studysyncandroid.ui.review

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ReviewScreen(
    deckId: String,
    viewModel: ReviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Trigger the initial data load when this screen opens
    LaunchedEffect(deckId) {
        viewModel.loadDueCards(deckId)
    }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator()
                }
                uiState.error != null -> {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
                uiState.hasNoDueCards -> {
                    Text(
                        text = "You're all caught up! No cards due right now.",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                }
                uiState.isReviewFinished -> {
                    Text(
                        text = "Awesome job! You finished your review session.",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                }
                else -> {
                    // Active Review State
                    uiState.currentCard?.let { card ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Card ${uiState.currentCardIndex + 1} of ${uiState.dueCards.size}",
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .padding(bottom = 24.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = card.question,
                                        style = MaterialTheme.typography.headlineMedium,
                                        textAlign = TextAlign.Center
                                    )

                                    if (uiState.isAnswerRevealed) {
                                        HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))
                                        Text(
                                            text = card.answer,
                                            style = MaterialTheme.typography.bodyLarge,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }

                            if (!uiState.isAnswerRevealed) {
                                Button(
                                    onClick = { viewModel.revealAnswer() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp)
                                ) {
                                    Text("Reveal Answer")
                                }
                            } else {
                                Text(
                                    text = "How well did you remember?",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )

                                // Simplified SM-2 Rating Buttons mapped to 0-5 quality
                                // Simplified SM-2 Rating Buttons mapped to 0-5 quality
                                Row(
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    RatingButton(label = "Blackout", quality = 0) { viewModel.submitRating(0) }
                                    RatingButton(label = "Wrong", quality = 1) { viewModel.submitRating(1) }
                                    RatingButton(label = "Hard", quality = 2) { viewModel.submitRating(2) }
                                    RatingButton(label = "Pass", quality = 3) { viewModel.submitRating(3) }
                                    RatingButton(label = "Good", quality = 4) { viewModel.submitRating(4) }
                                    RatingButton(label = "Easy", quality = 5) { viewModel.submitRating(5) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RatingButton(label: String, quality: Int, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        ElevatedButton(onClick = onClick) {
            Text(quality.toString())
        }
        Text(text = label, style = MaterialTheme.typography.labelSmall)
    }
}