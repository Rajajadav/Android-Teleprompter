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
        primary = Teal80,
        onPrimary = Color(0xFF042F2E),
        primaryContainer = TealDark40,
        onPrimaryContainer = TealLight80,
        secondary = TealCyan80,
        onSecondary = Color(0xFF082F49),
        secondaryContainer = Color(0xFF0369A1),
        onSecondaryContainer = Color(0xFFE0F2FE),
        tertiary = TealLight80,
        background = CharcoalBackground,
        onBackground = TextPrimary,
        surface = CharcoalSurface,
        onSurface = TextPrimary,
        surfaceVariant = CharcoalSurfaceVariant,
        onSurfaceVariant = TextSecondary,
        outline = CharcoalOutline,
        outlineVariant = CharcoalOutlineVariant,
        error = RecordRed,
        onError = Color.White
    )

private val LightColorScheme =
    lightColorScheme(
        primary = Teal40,
        onPrimary = Color.White,
        primaryContainer = Color(0xFFCCFBF1),
        onPrimaryContainer = Color(0xFF134E4A),
        secondary = TealCyan40,
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFE0F2FE),
        onSecondaryContainer = Color(0xFF0369A1),
        tertiary = TealDark40,
        background = Color(0xFFF8FAFC),
        onBackground = Color(0xFF0F172A),
        surface = Color.White,
        onSurface = Color(0xFF0F172A),
        surfaceVariant = Color(0xFFF1F5F9),
        onSurfaceVariant = Color(0xFF475569),
        outline = Color(0xFFCBD5E1),
        outlineVariant = Color(0xFFE2E8F0),
        error = Color(0xFFDC2626),
        onError = Color.White
    )

@Composable
fun PromptDeskTheme(
    darkTheme: Boolean = true, // Dark-first for teleprompter creator focus
    dynamicColor: Boolean = false, // Keep signature brand teal by default
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

