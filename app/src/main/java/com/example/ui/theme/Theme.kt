package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CamDirectorColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.Black,
    primaryContainer = Slate800,
    onPrimaryContainer = EmeraldLight,
    secondary = CyanAccent,
    onSecondary = Color.Black,
    secondaryContainer = Slate800,
    onSecondaryContainer = CyanAccent,
    tertiary = GoldAccent,
    background = DarkSurface,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary
)

@Composable
fun CamDirectorTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CamDirectorColorScheme,
        typography = Typography,
        content = content
    )
}
