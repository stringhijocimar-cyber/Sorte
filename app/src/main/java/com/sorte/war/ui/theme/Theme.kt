package com.sorte.war.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val WarColors = darkColorScheme(
    primary = Gold,
    onPrimary = NightNavy,
    secondary = GoldDeep,
    onSecondary = NightNavy,
    background = NightNavy,
    onBackground = TextPrimary,
    surface = PanelNavy,
    onSurface = TextPrimary,
    surfaceVariant = PanelNavyLight,
    onSurfaceVariant = TextSecondary,
    error = Crimson,
    outline = Divider
)

@Composable
fun WarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = WarColors,
        typography = WarTypography,
        content = content
    )
}
