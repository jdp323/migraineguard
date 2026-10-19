package com.migraineguard.app.core.design

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.migraineguard.app.core.domain.ThemeMode

val MigraineGuardPainColor = Color(0xFFE15C76)
private val LightColors = lightColorScheme(
    primary = Color(0xFF5954C7), onPrimary = Color.White,
    primaryContainer = Color(0xFFEEECFF), onPrimaryContainer = Color(0xFF302979),
    secondary = Color(0xFF147C72), onSecondary = Color.White,
    secondaryContainer = Color(0xFFDDF6EF), onSecondaryContainer = Color(0xFF14574F),
    tertiary = Color(0xFF9F5E45), onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFEBDF), onTertiaryContainer = Color(0xFF65351F),
    background = Color(0xFFF5F5FA), onBackground = Color(0xFF202039),
    surface = Color.White, onSurface = Color(0xFF202039),
    surfaceVariant = Color(0xFFEEEDF5), onSurfaceVariant = Color(0xFF68677D),
    outline = Color(0xFF8C899F), outlineVariant = Color(0xFFE4E1ED),
    error = Color(0xFFBA3450),
)
private val DarkColors = darkColorScheme(
    primary = Color(0xFFC1BCFF), onPrimary = Color(0xFF302979),
    primaryContainer = Color(0xFF35305E), onPrimaryContainer = Color(0xFFE8E5FF),
    secondary = Color(0xFF8DDCC9), onSecondary = Color(0xFF143C35),
    secondaryContainer = Color(0xFF244B44), onSecondaryContainer = Color(0xFFCBF6EA),
    tertiary = Color(0xFFF3BEA1), onTertiary = Color(0xFF4A2F22),
    tertiaryContainer = Color(0xFF543D32), onTertiaryContainer = Color(0xFFFFE3D2),
    background = Color(0xFF12121D), onBackground = Color(0xFFF0EDF7),
    surface = Color(0xFF1D1D2B), onSurface = Color(0xFFF0EDF7),
    surfaceVariant = Color(0xFF292836), onSurfaceVariant = Color(0xFFBBB7CB),
    outline = Color(0xFF938DA8), outlineVariant = Color(0xFF383445),
    error = Color(0xFFFFB1C0),
)

@Composable
fun MigraineGuardTheme(themeMode: ThemeMode = ThemeMode.SYSTEM, dynamicColor: Boolean = false,
    content: @Composable () -> Unit) {
    val dark = when (themeMode) { ThemeMode.SYSTEM -> isSystemInDarkTheme(); ThemeMode.DARK -> true; ThemeMode.LIGHT -> false }
    val colors = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (dark) dynamicDarkColorScheme(LocalContext.current) else dynamicLightColorScheme(LocalContext.current)
    } else if (dark) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, typography = MigraineGuardTypography,
        shapes = Shapes(small = RoundedCornerShape(12.dp), medium = RoundedCornerShape(20.dp),
            large = RoundedCornerShape(28.dp), extraLarge = RoundedCornerShape(36.dp)), content = content)
}
