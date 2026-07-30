package com.deskpet.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFD9E2),
    onPrimaryContainer = Color(0xFF5C0021),
    secondary = Secondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8E0FF),
    onSecondaryContainer = Color(0xFF2A1A4D),
    tertiary = Accent,
    onTertiary = Color(0xFF1B3B2B),
    tertiaryContainer = Color(0xFFD9F5E8),
    background = Bg,
    onBackground = Ink,
    surface = Surface,
    onSurface = Ink,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = Muted,
    surfaceTint = Primary,
    outline = Border,
    outlineVariant = Border,
    error = Danger,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002)
)

private val DarkColors = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC65377),
    onPrimaryContainer = Color(0xFFFFD9E2),
    secondary = Secondary,
    onSecondary = Color(0xFF2A1A4D),
    secondaryContainer = Color(0xFF3B2B5C),
    onSecondaryContainer = Color(0xFFE8E0FF),
    tertiary = Accent,
    onTertiary = Color(0xFF1B3B2B),
    tertiaryContainer = Color(0xFF2B5B44),
    onTertiaryContainer = Color(0xFFD9F5E8),
    background = DarkBg,
    onBackground = DarkInk,
    surface = DarkSurface,
    onSurface = DarkInk,
    surfaceVariant = Color(0xFF4A3F42),
    onSurfaceVariant = Color(0xFFD0C0C6),
    surfaceTint = Primary,
    outline = Color(0xFF7B6B70),
    outlineVariant = Color(0xFF4A3F42),
    error = Danger,
    onError = Color.White,
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

private val AppShapes = Shapes(
    extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
    small = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(32.dp)
)

/**
 * App-wide Material3 theme.
 *
 * @param darkTheme whether to use the dark color scheme (defaults to system).
 * @param dynamicColor reserved for future Material You support; disabled in this
 *   skeleton so the brand palette is always used.
 */
@Composable
fun DeskPetTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
