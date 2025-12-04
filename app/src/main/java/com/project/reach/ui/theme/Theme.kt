package com.project.reach.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
val grad = Brush.linearGradient(
    colors = listOf(
        Color(0x99F7A3C7), // 60%
        Color(0x99D6A7F7)

    ),
    start = Offset(0f, 0f),
    end = Offset(1000f, 1000f)
)
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFE4C1F9),      // Light violet/pink accent
    onPrimary = Color(0xFF121212),    // Dark text on primary
    secondary = Color(0xFFE4C1F9),    // Soft blue accent
    onSecondary = Color(0xFF7E7E80),
    background = Color(0xFF121212),   // Almost black
    onBackground = Color(0xFFEDEDED), // Light text
    surface = Color(0xFF121212),      // Same as background
    onSurface = Color(0xFFEDEDED),
    outline = Color(0xFF332C3F), // Subtle accent for borders
    outlineVariant = Color(0x1AEAEFEF),      // Same as background
    error = Color(0xFFFF6B6B),        // Soft red
    onError = Color(0xFFFFFFFF)       // White text on error
)

// ---------- Light Theme ----------
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6907A4),      // Light violet accent (same family as dark)
    onPrimary = Color(0xFF121212),    // Dark text on primary

    secondary = Color(0xFFCA9AE8),    // Soft blue accent (same as dark)
    onSecondary = Color(0xFF121212),

    background = Color(0xFFF4F4F8),   // Soft neutral light gray (NO yellow)
    onBackground = Color(0xFF121212), // High contrast dark text

    surface = Color(0xFFF4F4F8),      // Crisp white card surfaces
    onSurface = Color(0xFF121212),

    outline = Color(0xFFD8BEE8),
    outlineVariant = Color(0x1A000303),      // Same as background
    error = Color(0xFFFF6B6B),        // Same soft red as dark mode
    onError = Color(0xFFFFFFFF)
)

@Composable
fun REACHTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MultiFontTypography,
        content = content
    )
}
