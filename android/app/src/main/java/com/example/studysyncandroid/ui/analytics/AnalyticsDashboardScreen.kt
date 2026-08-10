package com.example.studysyncandroid.ui.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsDashboardScreen(
    onNavigateBack: () -> Unit,
    onRetentionCurveClick: () -> Unit,
    onLibraryStatusClick: () -> Unit,
    onUpcomingReviewsClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics Dashboard") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("<")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onRetentionCurveClick,
                modifier = Modifier.fillMaxWidth().height(60.dp)
            ) {
                Text("Retention Curve", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onLibraryStatusClick,
                modifier = Modifier.fillMaxWidth().height(60.dp)
            ) {
                Text("Library Status", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onUpcomingReviewsClick,
                modifier = Modifier.fillMaxWidth().height(60.dp)
            ) {
                Text("Upcoming Reviews", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
