package com.example.studysyncandroid.ui.analytics

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.studysyncandroid.R
import com.example.studysyncandroid.data.remote.dto.DailyRetentionDto
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun AnalyticsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val retentionData by viewModel.retentionData.collectAsState()
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
                        text = "Retention Curve", 
                        fontFamily = FontFamily.Serif, 
                        fontSize = 32.sp, 
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(imageVector = Icons.Default.TrendingUp, contentDescription = "Trending", tint = textSecondary, modifier = Modifier.size(28.dp))
                }
                Text(
                    text = "Visualize your memory strength and decay over the last 30 days. Consistent reviews help flatten the curve!",
                    fontSize = 16.sp,
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
                    retentionData.isNotEmpty() && retentionData.any { it.retentionPercentage != null } -> {
                        RetentionChart(retentionData)
                    }
                    else -> {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(imageVector = Icons.Default.TrendingUp, contentDescription = "Empty", tint = textSecondary.copy(alpha = 0.5f), modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No data available.", fontFamily = FontFamily.Serif, fontSize = 20.sp, color = textPrimary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Review some cards to start generating your retention curve!", fontSize = 14.sp, color = textSecondary, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RetentionChart(data: List<DailyRetentionDto>) {
    val cardBg = colorResource(id = R.color.deck_list_card_bg)
    val textPrimary = colorResource(id = R.color.deck_list_text_primary)
    val borderColor = colorResource(id = R.color.deck_list_border)

    val entries = data.mapIndexedNotNull { index, dto ->
        dto.retentionPercentage?.let {
            FloatEntry(x = index.toFloat(), y = it * 100f)
        }
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
                .padding(24.dp)
        ) {
            Text(
                text = "30-Day Curve",
                fontFamily = FontFamily.Serif,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = textPrimary,
                modifier = Modifier.padding(bottom = 24.dp)
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
                    .height(280.dp)
            )
        }
    }
}
