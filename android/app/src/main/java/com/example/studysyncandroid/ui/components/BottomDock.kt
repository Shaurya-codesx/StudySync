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
import androidx.compose.ui.unit.dp

@Composable
fun BottomDock(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    onCreateDeckClick: () -> Unit,
    onCreateFolderClick: () -> Unit
) {
    var showPlusMenu by remember { mutableStateOf(false) }

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
                        Text("Create Deck")
                    }
                    TextButton(
                        onClick = {
                            showPlusMenu = false
                            onCreateFolderClick()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Create Folder")
                    }
                }
            }
        }

        // The Dock
        Surface(
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
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
                    label = "Home",
                    isSelected = currentRoute == "deck_list",
                    onClick = {
                        showPlusMenu = false
                        onNavigate("deck_list")
                    }
                )
                
                DockItem(
                    icon = Icons.Default.Store,
                    label = "Market",
                    isSelected = currentRoute == "marketplace",
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
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable { showPlusMenu = !showPlusMenu },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }

                DockItem(
                    icon = Icons.Default.Group,
                    label = "Rooms",
                    isSelected = currentRoute == "rooms",
                    onClick = {
                        showPlusMenu = false
                        onNavigate("rooms")
                    }
                )

                DockItem(
                    icon = Icons.Default.Analytics,
                    label = "Analytics",
                    isSelected = currentRoute == "analytics",
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
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
    }
}
