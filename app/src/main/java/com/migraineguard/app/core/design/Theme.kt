package com.migraineguard.app.core.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Primary = Color(0xFF1B6CA8)
private val OnPrimary = Color(0xFFFFFFFF)
private val Secondary = Color(0xFF5B8DEF)
private val Tertiary = Color(0xFFE07A5F)
private val PainMarker = Color(0xFFE53935)

val MigraineGuardPainColor = PainMarker

private val LightColors = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    secondary = Secondary,
    tertiary = Tertiary,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF7BB8E8),
    onPrimary = Color(0xFF003258),
    secondary = Color(0xFFA8C5FF),
    tertiary = Color(0xFFFFB4A2),
)

@Composable
fun MigraineGuardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = MigraineGuardTypography,
        content = content,
    )
}
