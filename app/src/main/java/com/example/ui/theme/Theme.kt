package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = JokerGold,
    secondary = WisdomIndigo,
    tertiary = SuccessMint,
    background = MidnightBackground,
    surface = CardMidnight,
    onPrimary = MidnightBackground,
    onSecondary = LightText,
    onBackground = LightText,
    onSurface = LightText,
    error = ErrorRose
  )

private val LightColorScheme =
  darkColorScheme( // We enforce a gorgeous dark theme for both light/dark system toggle to preserve story book immersion!
    primary = JokerGold,
    secondary = WisdomIndigo,
    tertiary = SuccessMint,
    background = MidnightBackground,
    surface = CardMidnight,
    onPrimary = MidnightBackground,
    onSecondary = LightText,
    onBackground = LightText,
    onSurface = LightText,
    error = ErrorRose
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  // We use our signature Citadel Midnight theme exclusively to preserve aesthetic direction
  val colorScheme = DarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
