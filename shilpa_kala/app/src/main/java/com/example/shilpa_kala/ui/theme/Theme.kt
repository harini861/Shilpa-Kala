package com.example.shilpa_kala.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = DeepGold,
    secondary = Saffron,
    tertiary = Terracotta,
    background = DarkBackground,
    surface = Color(0xFF2D241E),
    onPrimary = Mahogany,
    onSecondary = Mahogany,
    onBackground = WarmBeige,
    onSurface = WarmBeige
)

private val LightColorScheme = lightColorScheme(
    primary = Mahogany,
    secondary = DeepGold,
    tertiary = Saffron,
    background = LightBackground,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Mahogany,
    onBackground = Mahogany,
    onSurface = Mahogany
)

@Composable
fun Shilpa_kalaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        val window = (view.context as Activity).window
        window.statusBarColor = colorScheme.primary.toArgb()
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
