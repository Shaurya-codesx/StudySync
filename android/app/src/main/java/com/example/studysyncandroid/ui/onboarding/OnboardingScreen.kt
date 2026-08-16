package com.example.studysyncandroid.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studysyncandroid.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToSignup: () -> Unit,
    onFinishOnboarding: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        containerColor = colorResource(id = R.color.deck_list_bg)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                when (page) {
                    0 -> ScreenOne()
                    1 -> ScreenTwo()
                    2 -> ScreenThree()
                    3 -> OnboardingFinalPage(
                        onLoginClick = {
                            onFinishOnboarding()
                            onNavigateToLogin()
                        },
                        onSignupClick = {
                            onFinishOnboarding()
                            onNavigateToSignup()
                        }
                    )
                }
            }

            // Pager Indicator & Navigation Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(4) { index ->
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(
                                    if (pagerState.currentPage == index) colorResource(id = R.color.deck_list_accent)
                                    else colorResource(id = R.color.deck_list_text_secondary).copy(alpha = 0.5f)
                                )
                        )
                    }
                }

                // Next Button
                if (pagerState.currentPage < 3) {
                    TextButton(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    ) {
                        Text(
                            text = "Next",
                            color = colorResource(id = R.color.deck_list_accent),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ScreenOne() {
    var phase by remember { mutableStateOf(0) } // 0: Text, 1: Transforming, 2: Deck

    LaunchedEffect(Unit) {
        delay(2500)
        phase = 1
        delay(800)
        phase = 2
    }

    val transition = rememberInfiniteTransition(label = "sparkles")
    val floatY1 by transition.animateFloat(
        initialValue = -15f,
        targetValue = 15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sparkle1"
    )
    val floatY2 by transition.animateFloat(
        initialValue = 15f,
        targetValue = -15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sparkle2"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            // Sparkles (Only in phase 0 and 1)
            androidx.compose.animation.AnimatedVisibility(
                visible = phase < 2,
                enter = fadeIn(),
                exit = fadeOut(animationSpec = tween(500)),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = colorResource(id = R.color.deck_list_accent),
                        modifier = Modifier
                            .size(30.dp)
                            .align(Alignment.TopStart)
                            .offset(y = floatY1.dp, x = 20.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = colorResource(id = R.color.deck_list_accent),
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.BottomEnd)
                            .offset(y = floatY2.dp, x = (-20).dp)
                    )
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = colorResource(id = R.color.deck_list_accent),
                        modifier = Modifier
                            .size(36.dp)
                            .align(Alignment.TopEnd)
                            .offset(y = floatY1.dp, x = (-40).dp)
                    )
                }
            }

            AnimatedContent(
                targetState = phase,
                transitionSpec = {
                    fadeIn(animationSpec = tween(800)) with fadeOut(animationSpec = tween(800))
                },
                label = "deckTransform"
            ) { currentPhase ->
                when (currentPhase) {
                    0 -> {
                        Box(
                            modifier = Modifier
                                .width(220.dp)
                                .height(130.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(colorResource(id = R.color.deck_list_card_bg))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Biology: Human Heart\n- 4 chambers\n- Pumps blood...",
                                color = colorResource(id = R.color.deck_list_text_secondary),
                                fontSize = 14.sp
                            )
                        }
                    }
                    else -> { // Phase 2 (Deck)
                        Card(
                            modifier = Modifier
                                .width(240.dp)
                                .rotate(-2f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, colorResource(id = R.color.deck_list_border)),
                            colors = CardDefaults.cardColors(containerColor = colorResource(id = R.color.deck_list_card_bg)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(20.dp)
                                    .fillMaxWidth()
                            ) {
                                HorizontalDivider(
                                    color = colorResource(id = R.color.deck_list_divider),
                                    thickness = 1.dp,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                                Text(
                                    text = "Biology 101",
                                    fontFamily = FontFamily.Serif,
                                    fontSize = 20.sp,
                                    color = colorResource(id = R.color.deck_list_text_primary),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "34 cards",
                                        fontSize = 14.sp,
                                        color = colorResource(id = R.color.deck_list_text_secondary)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Icon(
                                        Icons.Default.Public,
                                        contentDescription = null,
                                        tint = colorResource(id = R.color.deck_list_text_secondary),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Instant Flashcards with AI",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(id = R.color.deck_list_text_primary),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Don't waste time typing. Generate comprehensive study decks instantly from any topic, or discover public decks in the Marketplace.",
            fontSize = 16.sp,
            color = colorResource(id = R.color.deck_list_text_secondary),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}

@Composable
fun ScreenTwo() {
    val transition = rememberInfiniteTransition(label = "ring")
    
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ringRotation"
    )

    val scale by transition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ringScale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(180.dp)
                .scale(scale),
            contentAlignment = Alignment.Center
        ) {
            // Animated Ring
            CircularProgressIndicator(
                progress = { 0.75f },
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(rotation),
                color = colorResource(id = R.color.deck_list_accent),
                strokeWidth = 6.dp,
                trackColor = colorResource(id = R.color.deck_list_card_bg)
            )

            // Center Icon
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(colorResource(id = R.color.deck_list_card_bg)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.QueryStats,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    tint = colorResource(id = R.color.deck_list_accent)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Study Smarter, Not Harder",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(id = R.color.deck_list_text_primary),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Our spaced-repetition algorithm adapts to your memory. Track your mastery and let the app predict exactly what you need to review next.",
            fontSize = 16.sp,
            color = colorResource(id = R.color.deck_list_text_secondary),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}

@Composable
fun ScreenThree() {
    val transition = rememberInfiniteTransition(label = "pulse")
    
    val pulseScale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "timerPulse"
    )

    val pulseAlpha by transition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "timerAlpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(160.dp),
            contentAlignment = Alignment.Center
        ) {
            // Pulse Effect
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(pulseScale)
                    .alpha(pulseAlpha)
                    .clip(CircleShape)
                    .background(colorResource(id = R.color.deck_list_accent))
            )

            // Timer Icon
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(colorResource(id = R.color.deck_list_card_bg)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    tint = colorResource(id = R.color.deck_list_accent)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Deep Work & Live Focus",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(id = R.color.deck_list_text_primary),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Stay in the zone with custom Pomodoro timers, or join live study rooms to sync your focus sessions with others.",
            fontSize = 16.sp,
            color = colorResource(id = R.color.deck_list_text_secondary),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}

@Composable
fun OnboardingFinalPage(
    onLoginClick: () -> Unit,
    onSignupClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape)
                .background(colorResource(id = R.color.deck_list_card_bg)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Group,
                contentDescription = null,
                modifier = Modifier.size(70.dp),
                tint = colorResource(id = R.color.deck_list_accent)
            )
        }
        
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Ready to start?",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(id = R.color.deck_list_text_primary),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Join StudySync today and transform the way you learn.",
            fontSize = 16.sp,
            color = colorResource(id = R.color.deck_list_text_secondary),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onSignupClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colorResource(id = R.color.deck_list_accent))
        ) {
            Text(
                text = "Sign Up",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(id = R.color.deck_list_bg)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onLoginClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = colorResource(id = R.color.deck_list_accent)),
            border = BorderStroke(1.dp, colorResource(id = R.color.deck_list_accent))
        ) {
            Text(
                text = "Log In",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(id = R.color.deck_list_accent)
            )
        }
    }
}
