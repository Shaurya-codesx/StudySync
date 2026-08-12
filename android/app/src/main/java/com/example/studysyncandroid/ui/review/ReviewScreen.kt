package com.example.studysyncandroid.ui.review

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.studysyncandroid.R

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

    val bg = colorResource(id = R.color.deck_list_bg)
    val cardBg = colorResource(id = R.color.deck_list_card_bg)
    val textPrimary = colorResource(id = R.color.deck_list_text_primary)
    val textSecondary = colorResource(id = R.color.deck_list_text_secondary)
    val dividerColor = colorResource(id = R.color.deck_list_divider)
    val borderColor = colorResource(id = R.color.deck_list_border)

    Scaffold(
        containerColor = bg,
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(color = textSecondary)
                }
                uiState.error != null -> {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
                uiState.hasNoDueCards -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "You're all caught up!",
                            fontFamily = FontFamily.Serif,
                            fontSize = 24.sp,
                            color = textPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "No cards due right now.",
                            fontSize = 16.sp,
                            color = textSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                uiState.isReviewFinished -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Awesome job!",
                            fontFamily = FontFamily.Serif,
                            fontSize = 24.sp,
                            color = textPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "You finished your review session.",
                            fontSize = 16.sp,
                            color = textSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                else -> {
                    // Active Review State
                    uiState.currentCard?.let { card ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Top,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            // Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 24.dp, top = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Text(
                                    text = uiState.deckTitle ?: "Review",
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 24.sp,
                                    color = textPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "${uiState.currentCardIndex + 1} of ${uiState.dueCards.size}",
                                    fontSize = 14.sp,
                                    color = textSecondary,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }

                            // Flashcard
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .padding(bottom = 32.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = cardBg),
                                border = BorderStroke(1.dp, borderColor),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.Start
                                ) {
                                    // Red top line
                                    HorizontalDivider(
                                        color = dividerColor,
                                        thickness = 1.5.dp,
                                        modifier = Modifier.padding(bottom = 24.dp)
                                    )

                                    // Question
                                    Text(
                                        text = "Question",
                                        fontSize = 14.sp,
                                        color = textSecondary,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    Text(
                                        text = card.question,
                                        fontFamily = FontFamily.Serif,
                                        fontSize = 24.sp,
                                        color = textPrimary,
                                        lineHeight = 32.sp
                                    )

                                    if (uiState.isAnswerRevealed) {
                                        Spacer(Modifier.height(24.dp))
                                        
                                        // Dashed line
                                        val dashColor = borderColor
                                        Canvas(modifier = Modifier.fillMaxWidth().height(1.dp)) {
                                            drawLine(
                                                color = dashColor,
                                                start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                                end = androidx.compose.ui.geometry.Offset(size.width, 0f),
                                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                            )
                                        }

                                        Spacer(Modifier.height(24.dp))

                                        // Answer
                                        Text(
                                            text = "Answer",
                                            fontSize = 14.sp,
                                            color = textSecondary,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                        Text(
                                            text = card.answer,
                                            fontFamily = FontFamily.Serif,
                                            fontSize = 24.sp,
                                            color = textPrimary,
                                            lineHeight = 32.sp
                                        )
                                    }
                                }
                            }

                            // Bottom actions
                            if (!uiState.isAnswerRevealed) {
                                Button(
                                    onClick = { viewModel.revealAnswer() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = textPrimary),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Reveal Answer", fontSize = 16.sp, color = colorResource(id = R.color.white))
                                }
                                Spacer(Modifier.height(24.dp))
                            } else {
                                Text(
                                    text = "How well did you remember?",
                                    fontSize = 16.sp,
                                    color = textSecondary,
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )

                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                                ) {
                                    RatingButton(label = "Blackout", quality = 0, color = Color(0xFFC66262)) { viewModel.submitRating(0) }
                                    RatingButton(label = "Wrong", quality = 1, color = Color(0xFFD48A6A)) { viewModel.submitRating(1) }
                                    RatingButton(label = "Hard", quality = 2, color = Color(0xFFC9AD6A)) { viewModel.submitRating(2) }
                                    RatingButton(label = "Pass", quality = 3, color = Color(0xFFA6B86C)) { viewModel.submitRating(3) }
                                    RatingButton(label = "Good", quality = 4, color = Color(0xFF81C784)) { viewModel.submitRating(4) }
                                    RatingButton(label = "Perfect", quality = 5, color = Color(0xFF4CAF50)) { viewModel.submitRating(5) }
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
fun RatingButton(label: String, quality: Int, color: Color, onClick: () -> Unit) {
    val textPrimary = colorResource(id = R.color.deck_list_text_primary)
    val textSecondary = colorResource(id = R.color.deck_list_text_secondary)
    
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedButton(
            onClick = onClick,
            shape = CircleShape,
            border = BorderStroke(2.dp, color),
            modifier = Modifier.size(48.dp),
            contentPadding = PaddingValues(0.dp)
        ) {
            Text(
                text = quality.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = textSecondary
        )
    }
}