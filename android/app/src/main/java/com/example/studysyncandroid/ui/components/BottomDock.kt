package com.example.studysyncandroid.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.example.studysyncandroid.R

@Composable
fun BottomDock(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onCreateDeckClick: () -> Unit,
    onCreateFolderClick: () -> Unit
) {
    var showPlusMenu by remember { mutableStateOf(false) }

    val textPrimary = colorResource(id = R.color.deck_list_text_primary)
    val textSecondary = colorResource(id = R.color.deck_list_text_secondary)
    val cardBg = colorResource(id = R.color.deck_list_card_bg)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Pop-up Menu for Plus Button
        AnimatedVisibility(
            visible = showPlusMenu,
            enter = fadeIn() + expandVertically(expandFrom = Alignment.Bottom),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Bottom),
            modifier = Modifier.padding(bottom = 80.dp) // Sit above the dock
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                modifier = Modifier.width(200.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    TextButton(
                        onClick = {
                            showPlusMenu = false
                            onCreateDeckClick()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Create Deck", color = textPrimary)
                    }
                    TextButton(
                        onClick = {
                            showPlusMenu = false
                            onCreateFolderClick()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Create Folder", color = textPrimary)
                    }
                }
            }
        }

        // The Dock
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = Color(0xFFE8E9EC), // A very soft blueish-grey to match the screenshot
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DockItem(
                    icon = Icons.Default.Home,
                    isSelected = currentRoute == "deck_list",
                    selectedColor = textPrimary,
                    unselectedColor = textSecondary,
                    onClick = {
                        showPlusMenu = false
                        onNavigate("deck_list")
                    }
                )
                
                DockItem(
                    icon = Icons.Default.Store,
                    isSelected = currentRoute == "marketplace",
                    selectedColor = textPrimary,
                    unselectedColor = textSecondary,
                    onClick = {
                        showPlusMenu = false
                        onNavigate("marketplace")
                    }
                )

                // Plus Button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (showPlusMenu) textSecondary else textPrimary)
                        .clickable { showPlusMenu = !showPlusMenu },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        tint = colorResource(id = R.color.deck_list_bg)
                    )
                }

                DockItem(
                    icon = Icons.Default.Group,
                    isSelected = currentRoute == "rooms",
                    selectedColor = textPrimary,
                    unselectedColor = textSecondary,
                    onClick = {
                        showPlusMenu = false
                        onNavigate("rooms")
                    }
                )

                DockItem(
                    icon = Icons.Default.Analytics,
                    isSelected = currentRoute == "analytics",
                    selectedColor = textPrimary,
                    unselectedColor = textSecondary,
                    onClick = {
                        showPlusMenu = false
                        onNavigate("analytics")
                    }
                )
            }
        }
    }
}

@Composable
fun DockItem(
    icon: ImageVector,
    isSelected: Boolean,
    selectedColor: Color,
    unselectedColor: Color,
    onClick: () -> Unit
) {
    val tint = if (isSelected) selectedColor else unselectedColor
    Box(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(28.dp)
        )
    }
}
