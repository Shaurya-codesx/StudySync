package com.example.studysyncandroid.ui.analytics

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.studysyncandroid.data.remote.dto.LibraryStatusDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryStatusScreen(
    onNavigateBack: () -> Unit,
    viewModel: LibraryStatusViewModel = hiltViewModel()
) {
    val statusData by viewModel.libraryStatus.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Library Status") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("<")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading -> CircularProgressIndicator()
                errorMessage != null -> Text(text = errorMessage ?: "", color = MaterialTheme.colorScheme.error)
                statusData != null -> LibraryStatusDonutChart(statusData!!)
                else -> Text("No data available.")
            }
        }
    }
}

@Composable
fun LibraryStatusDonutChart(data: LibraryStatusDto) {
    if (data.totalCards == 0) {
        Text("Your library is empty. Generate or import some cards!")
        return
    }

    val newColor = Color(0xFF2196F3) // Blue
    val learningColor = Color(0xFFFF9800) // Orange
    val matureColor = Color(0xFF4CAF50) // Green

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(250.dp)
        ) {
            Canvas(modifier = Modifier.size(200.dp)) {
                val strokeWidth = 40.dp.toPx()
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
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Total Cards",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Legend
        LegendItem("New", data.newCards, data.totalCards, newColor)
        LegendItem("Learning", data.learningCards, data.totalCards, learningColor)
        LegendItem("Mature", data.matureCards, data.totalCards, matureColor)
    }
}

@Composable
fun LegendItem(label: String, count: Int, total: Int, color: Color) {
    val percentage = if (total > 0) (count.toFloat() / total) * 100 else 0f
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(16.dp),
                shape = MaterialTheme.shapes.small,
                color = color
            ) {}
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.bodyLarge)
        }
        Text(
            text = "$count (${String.format("%.1f", percentage)}%)",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
