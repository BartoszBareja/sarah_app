package com.example.sarah_app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AppColorScheme = lightColorScheme(
    primary = PrimaryOrange,
    onPrimary = Color.White,
    secondary = AccentOrange,
    onSecondary = Color.White,
    background = BackgroundCream,
    onBackground = BodyText,
    surface = SurfaceLight,
    onSurface = BrownText,
    surfaceVariant = SurfaceLight,
    onSurfaceVariant = BrownText,
)

@Composable
fun Sarah_appTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = Typography,
        content = content,
    )
}
