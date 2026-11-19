package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val PortalDarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = NavyDeepest,
    primaryContainer = Color(0xFF0A334D),
    onPrimaryContainer = NeonCyan,
    secondary = NeonPurple,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF2D164E),
    onSecondaryContainer = Color(0xFFE9D5FF),
    tertiary = NeonBlue,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF0E2B57),
    onTertiaryContainer = Color(0xFFBAE6FD),
    background = NavyDark,
    onBackground = TextPrimary,
    surface = NavySurface,
    onSurface = TextPrimary,
    surfaceVariant = NavySurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = NavyBorder,
    outlineVariant = Color(0xFF17264D),
    error = CrimsonAlert,
    onError = Color.White
)

private val PortalLightColorScheme = lightColorScheme(
    primary = Color(0xFF0077B6),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCFFAFE),
    onPrimaryContainer = Color(0xFF083344),
    secondary = NeonPurpleDim,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF3E8FF),
    onSecondaryContainer = Color(0xFF3B0764),
    tertiary = NeonBlue,
    onTertiary = Color.White,
    background = Color(0xFFF0F4FA),
    onBackground = Color(0xFF0B132B),
    surface = Color.White,
    onSurface = Color(0xFF0B132B),
    surfaceVariant = Color(0xFFE2E8F0),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
    error = CrimsonAlert,
    onError = Color.White
)

val PortalShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun PortalSamTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) PortalDarkColorScheme else PortalLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = PortalSamTypography,
        shapes = PortalShapes,
        content = content
    )
}
