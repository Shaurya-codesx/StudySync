package com.example.studysyncandroid.ui.analytics

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studysyncandroid.R

@Composable
fun AnalyticsDashboardScreen(
    onNavigateBack: () -> Unit,
    onRetentionCurveClick: () -> Unit,
    onLibraryStatusClick: () -> Unit,
    onUpcomingReviewsClick: () -> Unit
) {
    val bg = colorResource(id = R.color.deck_list_bg)
    val textPrimary = colorResource(id = R.color.deck_list_text_primary)
    val textSecondary = colorResource(id = R.color.deck_list_text_secondary)

    Scaffold(
        containerColor = bg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Custom Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack, modifier = Modifier.padding(end = 8.dp).offset(x = (-12).dp)) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = textPrimary)
                }
                Text(
                    text = "Analytics", 
                    fontFamily = FontFamily.Serif, 
                    fontSize = 32.sp, 
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Icon(imageVector = Icons.Default.Insights, contentDescription = "Insights", tint = textSecondary, modifier = Modifier.size(28.dp))
            }
            
            Text(
                text = "Gain insights into your study habits and track your learning progress over time.",
                fontSize = 16.sp,
                color = textSecondary,
                modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
            )

            // Navigation Cards
            AnalyticsNavigationCard(
                icon = Icons.Default.TrendingUp,
                title = "Retention Curve",
                description = "Visualize your memory strength and decay over time.",
                onClick = onRetentionCurveClick
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            AnalyticsNavigationCard(
                icon = Icons.Default.LibraryBooks,
                title = "Library Status",
                description = "Get a comprehensive overview of all your decks and cards.",
                onClick = onLibraryStatusClick
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            AnalyticsNavigationCard(
                icon = Icons.Default.Event,
                title = "Upcoming Reviews",
                description = "See what's due next and plan your upcoming study sessions.",
                onClick = onUpcomingReviewsClick
            )
        }
    }
}

@Composable
fun AnalyticsNavigationCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    val cardBg = colorResource(id = R.color.deck_list_card_bg)
    val textPrimary = colorResource(id = R.color.deck_list_text_primary)
    val textSecondary = colorResource(id = R.color.deck_list_text_secondary)
    val borderColor = colorResource(id = R.color.deck_list_border)
    val accent = colorResource(id = R.color.deck_list_accent)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Container
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(accent.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon, 
                    contentDescription = title, 
                    tint = accent, 
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(20.dp))
            
            // Text Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title, 
                    fontFamily = FontFamily.Serif,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = description, 
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = textSecondary
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Chevron
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Go",
                tint = textSecondary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
