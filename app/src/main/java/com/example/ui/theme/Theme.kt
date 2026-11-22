package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = Cyan400,
    onPrimary = DeepNavy950,
    primaryContainer = RoyalBlue700,
    onPrimaryContainer = PureWhite,
    secondary = Emerald400,
    onSecondary = DeepNavy950,
    secondaryContainer = Emerald600,
    onSecondaryContainer = PureWhite,
    tertiary = Gold400,
    onTertiary = DeepNavy950,
    tertiaryContainer = Purple700,
    onTertiaryContainer = PureWhite,
    background = DeepNavy950,
    onBackground = PureWhite,
    surface = DeepNavy900,
    onSurface = PureWhite,
    surfaceVariant = DeepNavy800,
    onSurfaceVariant = Slate200,
    outline = Slate700,
    error = Crimson500,
    onError = PureWhite
)

private val LightColorScheme = lightColorScheme(
    primary = RoyalBlue600,
    onPrimary = PureWhite,
    primaryContainer = DeepNavy900,
    onPrimaryContainer = PureWhite,
    secondary = Emerald600,
    onSecondary = PureWhite,
    secondaryContainer = ColorTokens.EmeraldSoft,
    onSecondaryContainer = Emerald600,
    tertiary = Gold600,
    onTertiary = PureWhite,
    tertiaryContainer = ColorTokens.GoldSoft,
    onTertiaryContainer = Gold600,
    background = Slate50,
    onBackground = Slate900,
    surface = PureWhite,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate600,
    outline = Slate200,
    error = Crimson600,
    onError = PureWhite
)

object ColorTokens {
    val EmeraldSoft = androidx.compose.ui.graphics.Color(0xFFD1FAE5)
    val GoldSoft = androidx.compose.ui.graphics.Color(0xFFFEF3C7)
    val CyanSoft = androidx.compose.ui.graphics.Color(0xFFCFFAFE)
    val PurpleSoft = androidx.compose.ui.graphics.Color(0xFFEDE9FE)
    val CrimsonSoft = androidx.compose.ui.graphics.Color(0xFFFEE2E2)
}

val AppShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun EFinanceRwandaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
