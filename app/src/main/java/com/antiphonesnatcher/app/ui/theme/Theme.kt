package com.antiphonesnatcher.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = AccentPillBackground,
    onPrimary = AccentPillText,
    surface = SurfaceCard,
    onSurface = TextPrimary,
    background = BackgroundDark,
    onBackground = TextPrimary,
    outline = BorderSubtle
)

@Composable
fun AntiPhoneSnatcherTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
