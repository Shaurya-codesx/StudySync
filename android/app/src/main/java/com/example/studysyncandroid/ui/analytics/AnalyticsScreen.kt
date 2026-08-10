package com.example.studysyncandroid.ui.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.studysyncandroid.data.remote.dto.DailyRetentionDto
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val retentionData by viewModel.retentionData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Learning Analytics") },
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
                retentionData.isNotEmpty() -> RetentionChart(retentionData)
                else -> Text("No data available.")
            }
        }
    }
}

@Composable
fun RetentionChart(data: List<DailyRetentionDto>) {
    val entries = data.mapIndexedNotNull { index, dto ->
        dto.retentionPercentage?.let {
            FloatEntry(x = index.toFloat(), y = it * 100f)
        }
    }

    if (entries.isEmpty()) {
        Text("No review data available in the last 30 days to plot.")
        return
    }

    val chartEntryModelProducer = remember { ChartEntryModelProducer(entries) }
    
    val bottomAxisValueFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
        val index = value.toInt()
        if (index in data.indices) {
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
            text = "30-Day Retention Curve",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Chart(
            chart = lineChart(),
            chartModelProducer = chartEntryModelProducer,
            startAxis = rememberStartAxis(
                valueFormatter = { value, _ -> "${value.toInt()}%" }
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
