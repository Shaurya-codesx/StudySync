package com.example.studysyncandroid.ui.analytics

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.studysyncandroid.R
import com.example.studysyncandroid.data.remote.dto.LibraryStatusDto

@Composable
fun LibraryStatusScreen(
    onNavigateBack: () -> Unit,
    viewModel: LibraryStatusViewModel = hiltViewModel()
) {
    val statusData by viewModel.libraryStatus.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val bg = colorResource(id = R.color.deck_list_bg)
    val textPrimary = colorResource(id = R.color.deck_list_text_primary)
    val textSecondary = colorResource(id = R.color.deck_list_text_secondary)
    val accent = colorResource(id = R.color.deck_list_accent)

    Scaffold(
        containerColor = bg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header Section
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.padding(end = 8.dp).offset(x = (-12).dp)) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = textPrimary)
                    }
                    Text(
                        text = "Library Status", 
                        fontFamily = FontFamily.Serif, 
                        fontSize = 32.sp, 
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(imageVector = Icons.Default.LibraryBooks, contentDescription = "Library", tint = textSecondary, modifier = Modifier.size(28.dp))
                }
                Text(
                    text = "Get a bird's-eye view of your entire flashcard collection. Track your learning progress by seeing how many cards are new, currently in learning, or fully matured in your memory.",
                    fontSize = 16.sp,
                    lineHeight = 22.sp,
                    color = textSecondary,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(color = accent, modifier = Modifier.align(Alignment.Center))
                    }
                    errorMessage != null -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = "Error", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(text = "Unable to load data", fontFamily = FontFamily.Serif, fontSize = 20.sp, color = textPrimary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = errorMessage ?: "Unknown error occurred.", fontSize = 14.sp, color = textSecondary, textAlign = TextAlign.Center)
                        }
                    }
                    statusData != null -> {
                        LibraryStatusDonutChart(statusData!!)
                    }
                    else -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(imageVector = Icons.Default.LibraryBooks, contentDescription = "Empty", tint = textSecondary.copy(alpha = 0.5f), modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No data available.", fontFamily = FontFamily.Serif, fontSize = 20.sp, color = textPrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LibraryStatusDonutChart(data: LibraryStatusDto) {
    val cardBg = colorResource(id = R.color.deck_list_card_bg)
    val textPrimary = colorResource(id = R.color.deck_list_text_primary)
    val textSecondary = colorResource(id = R.color.deck_list_text_secondary)
    val borderColor = colorResource(id = R.color.deck_list_border)
    val accent = colorResource(id = R.color.deck_list_accent)

    if (data.totalCards == 0) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = Icons.Default.LibraryBooks, contentDescription = "Empty", tint = textSecondary.copy(alpha = 0.5f), modifier = Modifier.size(64.dp).padding(bottom = 16.dp))
            Text("Your library is empty.", fontFamily = FontFamily.Serif, fontSize = 20.sp, color = textPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Generate or import some cards to see your status!", fontSize = 14.sp, color = textSecondary, textAlign = TextAlign.Center)
        }
        return
    }

    // Custom Nordic colors
    val newColor = accent // Muddy beige
    val learningColor = Color(0xFFA9B5C2) // Soft dusty blue
    val matureColor = Color(0xFF8B9A82) // Soft sage green

    val newAngle = (data.newCards.toFloat() / data.totalCards) * 360f
    val learningAngle = (data.learningCards.toFloat() / data.totalCards) * 360f
    val matureAngle = (data.matureCards.toFloat() / data.totalCards) * 360f

    // Animation state
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(data) {
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1500)
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Maturity Distribution",
                fontFamily = FontFamily.Serif,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = textPrimary,
                modifier = Modifier.padding(bottom = 8.dp).align(Alignment.Start)
            )
            
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(260.dp).padding(vertical = 16.dp)
            ) {
                Canvas(modifier = Modifier.size(220.dp)) {
                    val strokeWidth = 36.dp.toPx()
                    val sizeVal = size.minDimension
                    val topLeft = Offset((size.width - sizeVal) / 2f, (size.height - sizeVal) / 2f)
                    val arcSize = Size(sizeVal, sizeVal)

                    val currentProgress = animatedProgress.value
                    var startAngle = -90f // Start from top

                    // Draw New
                    val currentNewSweep = newAngle * currentProgress
                    if (currentNewSweep > 0) {
                        drawArc(
                            color = newColor,
                            startAngle = startAngle,
                            sweepAngle = currentNewSweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                        )
                        startAngle += currentNewSweep
                    }

                    // Draw Learning
                    val currentLearningSweep = learningAngle * currentProgress
                    if (currentLearningSweep > 0) {
                        drawArc(
                            color = learningColor,
                            startAngle = startAngle,
                            sweepAngle = currentLearningSweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                        )
                        startAngle += currentLearningSweep
                    }

                    // Draw Mature
                    val currentMatureSweep = matureAngle * currentProgress
                    if (currentMatureSweep > 0) {
                        drawArc(
                            color = matureColor,
                            startAngle = startAngle,
                            sweepAngle = currentMatureSweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                        )
                    }
                }

                // Center text
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${data.totalCards}",
                        fontFamily = FontFamily.Serif,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                    Text(
                        text = "Total Cards",
                        fontSize = 14.sp,
                        color = textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = borderColor, thickness = 1.dp, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp))

            // Legend
            LegendItem("New", data.newCards, data.totalCards, newColor, textPrimary, textSecondary)
            LegendItem("Learning", data.learningCards, data.totalCards, learningColor, textPrimary, textSecondary)
            LegendItem("Mature", data.matureCards, data.totalCards, matureColor, textPrimary, textSecondary)
        }
    }
}

@Composable
fun LegendItem(
    label: String, 
    count: Int, 
    total: Int, 
    color: Color,
    textPrimary: Color,
    textSecondary: Color
) {
    val percentage = if (total > 0) (count.toFloat() / total) * 100 else 0f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(14.dp),
                shape = CircleShape,
                color = color
            ) {}
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label, 
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = textPrimary
            )
        }
        Text(
            text = "$count (${String.format("%.1f", percentage)}%)",
            fontSize = 16.sp,
            color = textSecondary
        )
    }
}
