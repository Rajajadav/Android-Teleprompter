package com.example.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object ColorTokens {
    // Obsidian Dark Palette (from Stitch)
    val DarkPrimaryBg = Color(0xFF111317)
    val DarkSecondaryBg = Color(0xFF1A1C1F)
    val DarkCard = Color(0xFF1E2023)
    val DarkElevatedCard = Color(0xFF282A2D)
    val DarkCardHighest = Color(0xFF333538)
    val DarkCardLowest = Color(0xFF0C0E11)
    val DarkBorder = Color(0xFF252B32)
    val DarkOutline = Color(0xFF859490)
    val DarkOutlineVariant = Color(0xFF3C4A46)

    // Primary & Secondary (Mint Teal Luminescent)
    val PrimaryAccent = Color(0xFF4BE3CC)
    val PrimaryContainer = Color(0xFF19C7B1)
    val OnPrimary = Color(0xFF003730)
    val SecondaryAccent = Color(0xFF58DACA)
    val AccentGlow = Color(0x334BE3CC)
    val AccentSubtle = Color(0x1F4BE3CC)

    // Tertiary (Peach / Amber)
    val TertiaryAccent = Color(0xFFFFBFA2)
    val TertiaryContainer = Color(0xFFFF9763)

    // Typography
    val DarkPrimaryText = Color(0xFFE2E2E6)
    val DarkSecondaryText = Color(0xFFBBCAC5)
    val DarkMutedText = Color(0xFF6F7882)

    val Error = Color(0xFFFFB4AB)
    val Success = Color(0xFF38D39F)
    val Warning = Color(0xFFF5C451)

    // Light Palette
    val LightPrimaryBg = Color(0xFFF7F8FA)
    val LightSecondaryBg = Color(0xFFFFFFFF)
    val LightCard = Color(0xFFFFFFFF)
    val LightElevatedCard = Color(0xFFF1F3F5)
    val LightBorder = Color(0xFFE7EAED)

    val LightPrimaryAccent = Color(0xFF0BAF9D)
    val LightSecondaryAccent = Color(0xFF19C7B1)
    val LightPrimaryText = Color(0xFF111418)
    val LightSecondaryText = Color(0xFF626A73)
    val LightMutedText = Color(0xFF94A3B8)
}

object SpacingTokens {
    val xxs = 4.dp
    val xs = 8.dp
    val sm = 12.dp
    val md = 16.dp
    val lg = 20.dp
    val xl = 24.dp
    val xxl = 32.dp
    val xxxl = 40.dp
    val jumbo = 48.dp

    val ScreenHorizontal = 20.dp
    val CardPadding = 18.dp
    val BottomBarSpacing = 96.dp
}

object ShapeTokens {
    val Card = RoundedCornerShape(22.dp)
    val CardElevated = RoundedCornerShape(24.dp)
    val Button = RoundedCornerShape(16.dp)
    val BottomSheet = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    val Dialog = RoundedCornerShape(24.dp)
    val TextField = RoundedCornerShape(16.dp)
    val FloatingControl = RoundedCornerShape(18.dp)
    val Pill = RoundedCornerShape(12.dp)
    val Circular = CircleShape
}
