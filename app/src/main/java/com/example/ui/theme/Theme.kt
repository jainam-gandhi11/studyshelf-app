package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    secondary = DarkSecondary,
    tertiary = DarkTertiary,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = DarkBackground,       // High contrast text inside white buttons
    onSecondary = DarkPrimary,
    onTertiary = DarkOnSurface,
    onBackground = DarkOnBackground,
    onSurface = DarkOnSurface,
    surfaceContainer = DarkSurfaceCard
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    secondary = LightSecondary,
    tertiary = LightTertiary,
    background = LightBackground,
    surface = LightSurface,
    onPrimary = Color.White,          // White text inside black buttons
    onSecondary = LightPrimary,
    onTertiary = LightOnSurface,
    onBackground = LightOnBackground,
    onSurface = LightOnSurface,
    surfaceContainer = LightSurfaceCard
)

@Composable
fun StudyShelfTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Keep a backward compatible name so default previews don't break during compilations
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    StudyShelfTheme(darkTheme = darkTheme, content = content)
}
