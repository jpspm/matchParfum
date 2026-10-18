package com.matchparfum.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Warm amber / plum palette, reminiscent of perfume bottles.
private val LightColors = lightColorScheme(
    primary = Color(0xFF7A4F8C),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFF4DAFF),
    onPrimaryContainer = Color(0xFF300A45),
    secondary = Color(0xFF8A5A2B),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDCC0),
    onSecondaryContainer = Color(0xFF2E1500),
    tertiary = Color(0xFF4F6354),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFD1E8D4),
    onTertiaryContainer = Color(0xFF0C1F13),
    background = Color(0xFFFFF8F6),
    onBackground = Color(0xFF1F1A1C),
    surface = Color(0xFFFFF8F6),
    onSurface = Color(0xFF1F1A1C),
    surfaceVariant = Color(0xFFEFDFE8),
    onSurfaceVariant = Color(0xFF4D444B),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFFCF1F2),
    surfaceContainer = Color(0xFFF6EBEC),
    surfaceContainerHigh = Color(0xFFF0E5E6),
    surfaceContainerHighest = Color(0xFFEADFE1),
    outline = Color(0xFF7F747C),
    outlineVariant = Color(0xFFD1C3CC),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFE4B6F7),
    onPrimary = Color(0xFF46205A),
    primaryContainer = Color(0xFF5F3772),
    onPrimaryContainer = Color(0xFFF4DAFF),
    secondary = Color(0xFFFFB778),
    onSecondary = Color(0xFF4B2800),
    secondaryContainer = Color(0xFF6C4316),
    onSecondaryContainer = Color(0xFFFFDCC0),
    tertiary = Color(0xFFB5CCB9),
    onTertiary = Color(0xFF213527),
    tertiaryContainer = Color(0xFF374B3D),
    onTertiaryContainer = Color(0xFFD1E8D4),
    background = Color(0xFF171213),
    onBackground = Color(0xFFEBE0E2),
    surface = Color(0xFF171213),
    onSurface = Color(0xFFEBE0E2),
    surfaceVariant = Color(0xFF4D444B),
    onSurfaceVariant = Color(0xFFD1C3CC),
    surfaceContainerLowest = Color(0xFF120D0E),
    surfaceContainerLow = Color(0xFF1F1A1C),
    surfaceContainer = Color(0xFF241E20),
    surfaceContainerHigh = Color(0xFF2E282A),
    surfaceContainerHighest = Color(0xFF393335),
    outline = Color(0xFF9A8D96),
    outlineVariant = Color(0xFF4D444B),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
)

@Composable
fun MatchParfumTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
