package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SlendytubbiesDarkColorScheme =
  darkColorScheme(
    primary = AeroSkyMid,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF153854),
    onPrimaryContainer = SlendyInkLight,
    secondary = TubbyPurple,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF281C40),
    onSecondaryContainer = SlendyInkLight,
    tertiary = CustardPink,
    onTertiary = Color.White,
    background = SlendyNightBg,
    onBackground = SlendyInkLight,
    surface = SlendyNightSurface,
    onSurface = SlendyInkLight,
    surfaceVariant = Color(0xFF1B2B3B),
    onSurfaceVariant = SlendyInkMuted,
    outline = FrostBubbleBorder,
    error = ErrorCrimson,
    onError = Color.White,
  )

@Composable
fun SlendytubbiesAeroTheme(content: @Composable () -> Unit) {
  MaterialTheme(
    colorScheme = SlendytubbiesDarkColorScheme,
    typography = AppTypography,
    content = content,
  )
}
