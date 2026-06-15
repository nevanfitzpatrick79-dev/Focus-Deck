package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CosmicScheme = darkColorScheme(
    primary = CosmicPrimary,
    secondary = CosmicSecondary,
    tertiary = CosmicTertiary,
    background = CosmicBackground,
    surface = CosmicSurface,
    onPrimary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

private val ForestScheme = darkColorScheme(
    primary = ForestPrimary,
    secondary = ForestSecondary,
    tertiary = ForestTertiary,
    background = ForestBackground,
    surface = ForestSurface,
    onPrimary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

private val CyberScheme = darkColorScheme(
    primary = CyberPrimary,
    secondary = CyberSecondary,
    tertiary = CyberTertiary,
    background = CyberBackground,
    surface = CyberSurface,
    onPrimary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun FocusDeckTheme(
  themeName: String = "Cosmic Slate",
  content: @Composable () -> Unit,
) {
  val colorScheme = when(themeName) {
      "Forest Sanctuary" -> ForestScheme
      "Cyber Oasis" -> CyberScheme
      else -> CosmicScheme
  }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
