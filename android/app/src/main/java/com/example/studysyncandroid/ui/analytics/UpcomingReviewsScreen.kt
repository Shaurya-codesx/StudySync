package com.example.studysyncandroid.ui.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.studysyncandroid.data.remote.dto.UpcomingReviewDto
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.compose.component.lineComponent
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpcomingReviewsScreen(
    onNavigateBack: () -> Unit,
    viewModel: UpcomingReviewsViewModel = hiltViewModel()
) {
    val upcomingData by viewModel.upcomingReviews.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Upcoming Reviews") },
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
                upcomingData.isNotEmpty() -> UpcomingReviewsChart(upcomingData)
                else -> Text("No data available.")
            }
        }
    }
}

@Composable
fun UpcomingReviewsChart(data: List<UpcomingReviewDto>) {
    val entries = data.mapIndexed { index, dto ->
        FloatEntry(x = index.toFloat(), y = dto.cardsDue.toFloat())
    }

    val chartEntryModelProducer = remember { ChartEntryModelProducer(entries) }
    
    val bottomAxisValueFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
        val index = value.toInt()
        if (index == 0) {
            "Today"
        } else if (index in data.indices) {
            val dateStr = data[index].date
            try {
                val date = LocalDate.parse(dateStr)
                date.format(DateTimeFormatter.ofPattern("MMM dd"))
            } catch (e: Exception) {
                dateStr
            }
        } else {
            ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Next 10 Days Prediction",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Chart(
            chart = columnChart(
                columns = listOf(
                    lineComponent(
                        color = MaterialTheme.colorScheme.primary,
                        thickness = 36.dp
                    )
                ),
                spacing = 32.dp
            ),
            chartModelProducer = chartEntryModelProducer,
            startAxis = rememberStartAxis(
                valueFormatter = { value, _ -> "${value.toInt()}" }
            ),
            bottomAxis = rememberBottomAxis(
                valueFormatter = bottomAxisValueFormatter
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        )
    }
}
