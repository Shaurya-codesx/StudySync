package com.example.studysyncandroid.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.studysyncandroid.R

// Dock corner radius and the FAB's footprint — kept as constants so the
// notch shape and the button's offset always agree with each other.
private val DockCornerRadius = 32.dp
private val DockHeight = 60.dp
private val FabSize = 56.dp
private val NotchGap = 6.dp // visible gap between the FAB and the notch's edge
private val NotchRadius = FabSize / 2 + NotchGap

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

    // New theme colors — add these to colors.xml, snippet given separately
    val dockBg = colorResource(id = R.color.dock_bg)
    val dockAccent = colorResource(id = R.color.dock_accent)
    val dockActivePill = colorResource(id = R.color.dock_active_pill)
    val dockIconOnAccent = colorResource(id = R.color.dock_icon_on_accent)

    val dockShape = remember { DockShape(cornerRadius = DockCornerRadius, notchRadius = NotchRadius) }

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
            modifier = Modifier.padding(bottom = 96.dp) // sits above the dock + floating plus
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

        // The Dock — cream surface, rust accent border, with a carved notch
        // in the top edge that curves around the floating plus button.
        Surface(
            shape = dockShape,
            color = dockBg,
            shadowElevation = 8.dp,
            border = BorderStroke(1.5.dp, dockAccent.copy(alpha = 0.55f)),
            modifier = Modifier
                .fillMaxWidth()
                .height(DockHeight)
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
                    selectedColor = textPrimary,
                    unselectedColor = textSecondary,
                    activePillColor = dockActivePill,
                    onClick = {
                        showPlusMenu = false
                        onNavigate("deck_list")
                    }
                )

                DockItem(
                    icon = Icons.Default.Store,
                    label = "Market",
                    isSelected = currentRoute == "marketplace",
                    selectedColor = textPrimary,
                    unselectedColor = textSecondary,
                    activePillColor = dockActivePill,
                    onClick = {
                        showPlusMenu = false
                        onNavigate("marketplace")
                    }
                )

                // Empty space under the notch — the actual Plus button floats
                // above, positioned outside this Row. Surface clips content to
                // dockShape, so nothing draws in the cut-out area regardless.
                Spacer(modifier = Modifier.width(FabSize))

                DockItem(
                    icon = Icons.Default.Group,
                    label = "Study room",
                    isSelected = currentRoute == "rooms",
                    selectedColor = textPrimary,
                    unselectedColor = textSecondary,
                    activePillColor = dockActivePill,
                    onClick = {
                        showPlusMenu = false
                        onNavigate("rooms")
                    }
                )

                DockItem(
                    icon = Icons.Default.Analytics,
                    label = "Stats",
                    isSelected = currentRoute == "analytics",
                    selectedColor = textPrimary,
                    unselectedColor = textSecondary,
                    activePillColor = dockActivePill,
                    onClick = {
                        showPlusMenu = false
                        onNavigate("analytics")
                    }
                )
            }
        }

        // Floating Plus button — its center sits exactly on the dock's top
        // edge, so half pokes above and half tucks into the notch, leaving
        // NotchGap of dock color visible as a ring around it.
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = -(DockHeight - FabSize / 2))
                .size(FabSize)
                .clip(CircleShape)
                .background(dockAccent)
                .clickable { showPlusMenu = !showPlusMenu },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                tint = dockIconOnAccent
            )
        }
    }
}

@Composable
fun DockItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    selectedColor: Color,
    unselectedColor: Color,
    activePillColor: Color,
    onClick: () -> Unit
) {
    val tint = if (isSelected) selectedColor else unselectedColor
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(45.dp)
                .clip(CircleShape)
                .background(if (isSelected) activePillColor else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

/**
 * A rounded-rect shape with a smooth, shallow notch carved into the top
 * edge, centered horizontally — used to let the floating plus button sit
 * "hugged" by the dock instead of just overlapping it.
 */
private class DockShape(
    private val cornerRadius: Dp,
    private val notchRadius: Dp
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            with(density) {
                val corner = cornerRadius.toPx()
                val curveR = notchRadius.toPx()
                val cx = size.width / 2f

                moveTo(corner, 0f)
                lineTo(cx - curveR, 0f)

                // Dip down and back up around the notch — two cubic curves
                // give a shallower, smoother valley than a true semicircle.
                cubicTo(
                    cx - curveR, 0f,
                    cx - curveR * 0.55f, curveR,
                    cx, curveR
                )
                cubicTo(
                    cx + curveR * 0.55f, curveR,
                    cx + curveR, 0f,
                    cx + curveR, 0f
                )

                lineTo(size.width - corner, 0f)
                arcTo(Rect(size.width - 2 * corner, 0f, size.width, 2 * corner), -90f, 90f, false)
                lineTo(size.width, size.height - corner)
                arcTo(Rect(size.width - 2 * corner, size.height - 2 * corner, size.width, size.height), 0f, 90f, false)
                lineTo(corner, size.height)
                arcTo(Rect(0f, size.height - 2 * corner, 2 * corner, size.height), 90f, 90f, false)
                lineTo(0f, corner)
                arcTo(Rect(0f, 0f, 2 * corner, 2 * corner), 180f, 90f, false)
                close()
            }
        }
        return Outline.Generic(path)
    }
}