package com.migraineguard.app.core.design

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.migraineguard.app.core.domain.ThemeMode

val MigraineGuardPainColor = Color(0xFFE15C76)
private val LightColors = lightColorScheme(
    primary = Color(0xFF4D55CC), onPrimary = Color.White,
    primaryContainer = Color(0xFFE9EDFF), onPrimaryContainer = Color(0xFF293779),
    secondary = Color(0xFF147C72), onSecondary = Color.White,
    secondaryContainer = Color(0xFFDDF6EF), onSecondaryContainer = Color(0xFF14574F),
    tertiary = Color(0xFF9F5E45), onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFEBDF), onTertiaryContainer = Color(0xFF65351F),
    background = Color(0xFFF3F6FB), onBackground = Color(0xFF18243B),
    surface = Color.White, onSurface = Color(0xFF18243B),
    surfaceVariant = Color(0xFFEAF0F7), onSurfaceVariant = Color(0xFF5E6A80),
    outline = Color(0xFF8C899F), outlineVariant = Color(0xFFDAE2EE),
    surfaceContainerLowest = Color.White, surfaceContainerLow = Color(0xFFF1F0F9),
    surfaceContainer = Color(0xFFEEEDF7), surfaceContainerHigh = Color(0xFFE9E7F3),
    error = Color(0xFFBA3450),
)
private val DarkColors = darkColorScheme(
    primary = Color(0xFFB7C2FF), onPrimary = Color(0xFF293779),
    primaryContainer = Color(0xFF2D3A63), onPrimaryContainer = Color(0xFFE8E5FF),
    secondary = Color(0xFF8DDCC9), onSecondary = Color(0xFF143C35),
    secondaryContainer = Color(0xFF244B44), onSecondaryContainer = Color(0xFFCBF6EA),
    tertiary = Color(0xFFF3BEA1), onTertiary = Color(0xFF4A2F22),
    tertiaryContainer = Color(0xFF543D32), onTertiaryContainer = Color(0xFFFFE3D2),
    background = Color(0xFF0C1322), onBackground = Color(0xFFF0EDF7),
    surface = Color(0xFF172237), onSurface = Color(0xFFF0EDF7),
    surfaceVariant = Color(0xFF223047), onSurfaceVariant = Color(0xFFADBBD0),
    outline = Color(0xFF938DA8), outlineVariant = Color(0xFF35445C),
    surfaceContainerLowest = Color(0xFF0C1020), surfaceContainerLow = Color(0xFF161C30),
    surfaceContainer = Color(0xFF1F273E), surfaceContainerHigh = Color(0xFF293149),
    error = Color(0xFFFFB1C0),
)

@Composable
fun MigraineGuardTheme(themeMode: ThemeMode = ThemeMode.SYSTEM, dynamicColor: Boolean = false,
    reduceTransparency: Boolean = false, content: @Composable () -> Unit) {
    val dark = when (themeMode) { ThemeMode.SYSTEM -> isSystemInDarkTheme(); ThemeMode.DARK -> true; ThemeMode.LIGHT -> false }
    val colors = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (dark) dynamicDarkColorScheme(LocalContext.current) else dynamicLightColorScheme(LocalContext.current)
    } else if (dark) DarkColors else LightColors
    CompositionLocalProvider(LocalReduceTransparency provides reduceTransparency) {
    MaterialTheme(colorScheme = colors.copy(background = Color.Transparent), typography = MigraineGuardTypography,
        shapes = Shapes(small = RoundedCornerShape(12.dp), medium = RoundedCornerShape(20.dp),
            large = RoundedCornerShape(26.dp), extraLarge = RoundedCornerShape(32.dp))) { GlassBackdrop(dark, content) }
    }
}
