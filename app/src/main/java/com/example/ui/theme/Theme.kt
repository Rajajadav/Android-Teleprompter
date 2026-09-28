package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
    darkColorScheme(
        primary = ColorTokens.PrimaryAccent,
        onPrimary = Color(0xFF042F2E),
        primaryContainer = ColorTokens.AccentSubtle,
        onPrimaryContainer = ColorTokens.SecondaryAccent,
        secondary = ColorTokens.SecondaryAccent,
        onSecondary = Color(0xFF082F49),
        secondaryContainer = ColorTokens.DarkElevatedCard,
        onSecondaryContainer = ColorTokens.DarkPrimaryText,
        background = ColorTokens.DarkPrimaryBg,
        onBackground = ColorTokens.DarkPrimaryText,
        surface = ColorTokens.DarkSecondaryBg,
        onSurface = ColorTokens.DarkPrimaryText,
        surfaceVariant = ColorTokens.DarkCard,
        onSurfaceVariant = ColorTokens.DarkSecondaryText,
        outline = ColorTokens.DarkBorder,
        outlineVariant = ColorTokens.DarkBorder.copy(alpha = 0.5f),
        error = ColorTokens.Error,
        onError = Color.White
    )

private val LightColorScheme =
    lightColorScheme(
        primary = ColorTokens.LightPrimaryAccent,
        onPrimary = Color.White,
        primaryContainer = Color(0xFFE6FAF7),
        onPrimaryContainer = Color(0xFF044840),
        secondary = ColorTokens.LightSecondaryAccent,
        onSecondary = Color.White,
        secondaryContainer = ColorTokens.LightElevatedCard,
        onSecondaryContainer = ColorTokens.LightPrimaryText,
        background = ColorTokens.LightPrimaryBg,
        onBackground = ColorTokens.LightPrimaryText,
        surface = ColorTokens.LightSecondaryBg,
        onSurface = ColorTokens.LightPrimaryText,
        surfaceVariant = ColorTokens.LightCard,
        onSurfaceVariant = ColorTokens.LightSecondaryText,
        outline = ColorTokens.LightBorder,
        outlineVariant = ColorTokens.LightBorder.copy(alpha = 0.5f),
        error = ColorTokens.Error,
        onError = Color.White
    )

@Composable
fun PromptDeskTheme(
    darkTheme: Boolean = true, // Dark-first creator aesthetic
    dynamicColor: Boolean = false, // Preserve brand Teal palette
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            darkTheme -> DarkColorScheme
            else -> LightColorScheme
        }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

// Backwards compatibility alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    PromptDeskTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}

