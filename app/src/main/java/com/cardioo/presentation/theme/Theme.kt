package com.cardioo.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightScheme = lightColorScheme(
    primary = PinkPrimary,
    onPrimary = White,
    primaryContainer = PinkContainer,
    onPrimaryContainer = Black,
    secondary = PinkAccent,
    onSecondary = Black,
    background = White,
    onBackground = Black,
    surface = White,
    onSurface = Black,
)

private val DarkScheme = darkColorScheme(
    primary = PinkAccent,
    onPrimary = Black,
    primaryContainer = ColorTokens.DarkSurface,
    onPrimaryContainer = White,
    secondary = PinkPrimary,
    onSecondary = Black,
    background = ColorTokens.DarkBackground,
    onBackground = White,
    surface = ColorTokens.DarkSurface,
    onSurface = White,
)

@Composable
fun CardiooTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        typography = Typography,
        content = content,
    )
}

private object ColorTokens {
    val DarkBackground = androidx.compose.ui.graphics.Color(0xFF0F0F10)
    val DarkSurface = androidx.compose.ui.graphics.Color(0xFF17171A)
}

