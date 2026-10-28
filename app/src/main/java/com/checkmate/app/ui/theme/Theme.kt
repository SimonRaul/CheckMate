package com.checkmate.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val LightColors = lightColorScheme(
    primary = Blue600,
    onPrimary = Color.White,
    primaryContainer = Blue100,
    onPrimaryContainer = Navy700,
    secondary = Navy700,
    background = OffWhite,
    onBackground = Ink,
    surface = Surface,
    onSurface = Ink,
    surfaceVariant = OffWhite,
    onSurfaceVariant = InkMuted,
    surfaceContainerLowest = Surface,
    surfaceContainerLow = Surface,
    surfaceContainer = Surface,
    outline = Outline,
    outlineVariant = Outline,
    error = Danger,
)

private val CheckMateShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
)

/** The app is designed as a light, off-white UI with blue accents. */
@Composable
fun CheckMateTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = CheckMateTypography,
        shapes = CheckMateShapes,
        content = content,
    )
}
