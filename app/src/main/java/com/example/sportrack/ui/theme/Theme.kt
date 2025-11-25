package com.example.sportrack.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    // ГЛАВНЫЕ ЦВЕТА
    primary = SportGreen,
    onPrimary = Color.White, // Цвет текста НА зеленой кнопке

    secondary = SportBlue,
    onSecondary = Color.White,

    tertiary = SportYellow,

    // ФОНЫ
    background = DarkBackground,
    onBackground = Color.White, // Текст на фоне

    surface = DarkSurface,
    onSurface = Color.White     // Текст на карточках
)

private val LightColorScheme = lightColorScheme(
    primary = SportGreen,
    onPrimary = Color.White,

    secondary = SportBlue,
    onSecondary = Color.White,

    tertiary = SportYellow,

    background = LightBackground,
    onBackground = Color(0xFF3C3C3C), // Темно-серый текст на белом

    surface = LightSurface,
    onSurface = Color(0xFF3C3C3C)
)

@Composable
fun SportrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}