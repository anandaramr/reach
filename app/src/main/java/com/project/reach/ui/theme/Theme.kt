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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// ---------- Dark Theme ----------
// ---------- Dark Theme ----------
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFE4C1F9),      // Light violet/pink accent
    onPrimary = Color(0xFF121212),    // Dark text on primary
    secondary = Color(0xFFA9DEF9),    // Soft blue accent
    onSecondary = Color(0xFF121212),
    background = Color(0xFF121212),   // Almost black
    onBackground = Color(0xFFEDEDED), // Light text
    surface = Color(0xFF121212),      // Same as background
    onSurface = Color(0xFFEDEDED),
    outline = Color(0xFF6B7A7A),      // Subtle accent for borders
    error = Color(0xFFFF6B6B),        // Soft red
    onError = Color(0xFFFFFFFF)       // White text on error
)

// ---------- Light Theme ----------
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFFF99C8),      // Soft pink accent
    onPrimary = Color(0xFF011213),    // Dark text on primary
    secondary = Color(0xFFD0F4DE),    // Soft green accent
    onSecondary = Color(0xFF011213),
    background = Color(0xFFFCF6BD),   // Almost white / very light yellow
    onBackground = Color(0xFF011213), // Dark text
    surface = Color(0xFFFCF6BD),      // Same as background
    onSurface = Color(0xFF011213),
    outline = Color(0xFF818788),      // Light accent for borders
    error = Color(0xFFB00020),
    onError = Color(0xFFFFFFFF)
)

@Composable
fun REACHTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MultiFontTypography,
        content = content
    )
}