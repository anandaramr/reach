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

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFCBF2),
    onPrimary = Color.Black,
    secondary = Color(0xFFD8BBFF),
    onSecondary = Color.Black,
    background = Color(0xFF111111),
    onBackground = Color(0xFFC0FDFF),
    outline = Color(0xFFD0D0D0),
    surface = Color.Transparent,
    onSurface = Color(0xFFE2AFFF),
    error = Color(0xFFFF5555),
    onError = Color.Black
)
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2D0065),
    onPrimary = Color(0xFFFFFFFF),
    secondary = Color(0xFF360153),
    onSecondary = Color(0xFFFFFFFF),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF121212),
    outline = Color(0xFF151414),
    surface = Color.Transparent,
    onSurface = Color(0xFF121212),
    error = Color(0xFF600028),
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