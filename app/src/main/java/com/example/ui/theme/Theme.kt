package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NothingDarkColorScheme = darkColorScheme(
  primary = NothingWhite,
  onPrimary = NothingBlack,
  primaryContainer = NothingSurfaceElevated,
  onPrimaryContainer = NothingWhite,
  secondary = NothingRed,
  onSecondary = NothingWhite,
  secondaryContainer = NothingDarkGray,
  onSecondaryContainer = NothingWhite,
  tertiary = NothingGray,
  onTertiary = NothingWhite,
  background = NothingBlack,
  onBackground = NothingWhite,
  surface = NothingDarkSurface,
  onSurface = NothingWhite,
  surfaceVariant = NothingDarkGray,
  onSurfaceVariant = NothingGray,
  outline = NothingBorder,
  outlineVariant = NothingBorderSubtle,
  error = NothingRed
)

private val NothingLightColorScheme = lightColorScheme(
  primary = NothingBlack,
  onPrimary = NothingWhite,
  primaryContainer = Color(0xFFE8E8E8),
  onPrimaryContainer = NothingBlack,
  secondary = NothingRed,
  onSecondary = NothingWhite,
  secondaryContainer = Color(0xFFF2F2F2),
  onSecondaryContainer = NothingBlack,
  tertiary = NothingDarkGray,
  onTertiary = NothingWhite,
  background = BackgroundLight,
  onBackground = NothingBlack,
  surface = SurfaceLight,
  onSurface = NothingBlack,
  surfaceVariant = Color(0xFFE5E5E5),
  onSurfaceVariant = NothingDarkGray,
  outline = Color(0xFFCCCCCC),
  outlineVariant = Color(0x1F000000),
  error = NothingRed
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Default to Nothing OS signature pitch black
  dynamicColor: Boolean = false, // Keep iconic Nothing monochrome + red design
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) NothingDarkColorScheme else NothingLightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
