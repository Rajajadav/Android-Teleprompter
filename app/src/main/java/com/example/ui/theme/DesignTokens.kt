package com.example.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object ColorTokens {
    // Dark Palette (Default)
    val DarkPrimaryBg = Color(0xFF0B0D10)
    val DarkSecondaryBg = Color(0xFF12151A)
    val DarkCard = Color(0xFF171B21)
    val DarkElevatedCard = Color(0xFF1D2229)
    val DarkBorder = Color(0xFF252B32)

    val PrimaryAccent = Color(0xFF19C7B1) // Teal Green Signature
    val SecondaryAccent = Color(0xFF65E6D5)
    val AccentGlow = Color(0x3319C7B1)
    val AccentSubtle = Color(0x1F19C7B1)

    val DarkPrimaryText = Color(0xFFF5F7F8)
    val DarkSecondaryText = Color(0xFFA7AFB7)
    val DarkMutedText = Color(0xFF6F7882)

    val Error = Color(0xFFFF6B6B)
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
