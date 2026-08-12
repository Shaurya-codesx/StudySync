package com.example.studysyncandroid.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.studysyncandroid.R

// Manrope — geometric, warm, slightly rounded terminals. Used for headings,
// titles, numbers, and anywhere the UI needs quiet confidence.
val ManropeFamily = FontFamily(
    Font(R.font.manrope_regular, FontWeight.Normal),
    Font(R.font.manrope_medium, FontWeight.Medium),
    Font(R.font.manrope_semibold, FontWeight.SemiBold),
    Font(R.font.manrope_bold, FontWeight.Bold)
)

// Inter — the calmest, most legible grotesk for body copy and labels.
val InterFamily = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium)
)

/**
 * A small, deliberate type scale for the Nordic minimalist system.
 * Not the full MaterialTheme.typography — just the styles this redesign uses,
 * so screens stay clean and consistent without re-specifying letterSpacing
 * and weight every time.
 */
object NordicType {
    // Large screen title — "Your Decks"
    val screenTitle = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        letterSpacing = 0.2.sp
    )

    // Section header — "Folders", "Uncategorized Decks"
    val sectionLabel = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        letterSpacing = 1.6.sp
    )

    // Card / row title — deck name, folder name
    val cardTitle = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        letterSpacing = 0.1.sp
    )

    // Card metadata — "12 cards"
    val cardMeta = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.5.sp,
        letterSpacing = 0.3.sp
    )

    // Large numeral badge — card count shown big inside a deck row
    val numeralBadge = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        letterSpacing = 0.sp
    )

    // Dialog title
    val dialogTitle = TextStyle(
        fontFamily = ManropeFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 19.sp,
        letterSpacing = 0.1.sp
    )

    // Dialog body copy
    val dialogBody = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.5.sp,
        letterSpacing = 0.2.sp,
        lineHeight = 21.sp
    )

    // Buttons, menu items
    val button = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        letterSpacing = 0.4.sp
    )

    // Empty state copy
    val emptyState = TextStyle(
        fontFamily = InterFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        letterSpacing = 0.2.sp,
        lineHeight = 22.sp
    )
}