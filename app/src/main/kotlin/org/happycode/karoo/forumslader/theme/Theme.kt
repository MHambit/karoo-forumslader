package org.happycode.karoo.forumslader.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class ForumsladerCustomColors(
    val statusGood: Color,
    val statusWarning: Color,
    val statusDanger: Color,
    val accentPower: Color,
    val accentDistance: Color,
)

val LocalForumsladerCustomColors = staticCompositionLocalOf {
    ForumsladerCustomColors(
        statusGood = Color(0xFF16A34A),
        statusWarning = Color(0xFFEA580C),
        statusDanger = Color(0xFFDC2626),
        accentPower = Color(0xFFD97706),
        accentDistance = Color(0xFF0284C7)
    )
}

val MaterialTheme.forumsladerColors: ForumsladerCustomColors
    @Composable
    get() = LocalForumsladerCustomColors.current

// Crisp Modern Karoo — Pure Monochrome (Stealth) theme
// Neutral OLED-black / stark-white chrome so only telemetry status carries color.
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF18181B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF4F4F5),
    onPrimaryContainer = Color(0xFF18181B),
    secondary = Color(0xFF52525B),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE4E4E7),
    onSecondaryContainer = Color(0xFF18181B),
    surface = Color(0xFFF2F3F5),
    onSurface = Color(0xFF09090B),
    surfaceVariant = Color(0xFFFFFFFF),
    onSurfaceVariant = Color(0xFF52525B),
    error = Color(0xFFDC2626),
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = Color(0xFF7F1D1D),
    outline = Color(0xFF71717A),
    outlineVariant = Color(0xFFE4E4E7)
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFE4E4E7),
    onPrimary = Color(0xFF09090B),
    primaryContainer = Color(0xFF1F1F23),
    onPrimaryContainer = Color(0xFFF4F4F5),
    secondary = Color(0xFFA1A1AA),
    onSecondary = Color(0xFF09090B),
    secondaryContainer = Color(0xFF27272A),
    onSecondaryContainer = Color(0xFFE4E4E7),
    surface = Color(0xFF000000),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF141416),
    onSurfaceVariant = Color(0xFFA1A1AA),
    error = Color(0xFFF87171),
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFEE2E2),
    outline = Color(0xFF71717A),
    outlineVariant = Color(0xFF27272A)
)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val customColors = if (darkTheme) {
        ForumsladerCustomColors(
            statusGood = Color(0xFF22C55E),
            statusWarning = Color(0xFFF59E0B),
            statusDanger = Color(0xFFEF4444),
            accentPower = Color(0xFFF59E0B),
            accentDistance = Color(0xFF38BDF8)
        )
    } else {
        ForumsladerCustomColors(
            statusGood = Color(0xFF16A34A),
            statusWarning = Color(0xFFEA580C),
            statusDanger = Color(0xFFDC2626),
            accentPower = Color(0xFFD97706),
            accentDistance = Color(0xFF0284C7)
        )
    }

    CompositionLocalProvider(LocalForumsladerCustomColors provides customColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
