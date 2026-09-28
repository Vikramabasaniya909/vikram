package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = GymOrangePrimary,
    onPrimary = Color.White,
    primaryContainer = GymOrangePressed,
    onPrimaryContainer = Color.White,
    secondary = GymCyanSecondary,
    onSecondary = Color(0xFF080D18),
    tertiary = GymGreenSuccess,
    onTertiary = Color.White,
    background = GymDarkBackground,
    onBackground = GymTextPrimary,
    surface = GymDarkSurface,
    onSurface = GymTextPrimary,
    surfaceVariant = GymCardBackground,
    onSurfaceVariant = GymTextSecondary,
    outline = GymCardBorder,
    outlineVariant = GymDivider
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
