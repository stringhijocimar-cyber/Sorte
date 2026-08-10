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
    tertiary = TacticalTeal,
    onTertiary = NightNavy,
    background = NightNavy,
    onBackground = TextPrimary,
    surface = PanelNavy,
    onSurface = TextPrimary,
    surfaceVariant = PanelNavyLight,
    onSurfaceVariant = TextSecondary,
    error = Crimson,
    outline = TacticalStroke
)

@Composable
fun WarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // O jogo usa uma identidade escura própria; o parâmetro é mantido para compatibilidade.
    @Suppress("UNUSED_VARIABLE")
    val systemPrefersDark = darkTheme

    MaterialTheme(
        colorScheme = WarColors,
        typography = WarTypography,
        content = content
    )
}
