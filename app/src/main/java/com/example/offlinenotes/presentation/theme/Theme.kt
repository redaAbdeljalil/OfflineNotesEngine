package com.example.offlinenotes.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = Color.White,
    primaryContainer = SurfaceVariantLight,
    onPrimaryContainer = PrimaryLight,
    secondary = Info,
    onSecondary = Color.White,
    error = Error,
    onError = Color.White,
    background = Color.White,
    onBackground = PrimaryLight,
    surface = SurfaceLight,
    onSurface = PrimaryLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = PrimaryLight.copy(alpha = 0.7f)
)

private val DarkColors = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = Color.Black,
    primaryContainer = SurfaceVariantDark,
    onPrimaryContainer = PrimaryDark,
    secondary = Info,
    onSecondary = Color.White,
    error = Error,
    onError = Color.White,
    background = BackgroundDark,
    onBackground = PrimaryDark,
    surface = SurfaceDark,
    onSurface = PrimaryDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = PrimaryDark.copy(alpha = 0.8f)
)

@Composable
fun OfflineNotesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}
