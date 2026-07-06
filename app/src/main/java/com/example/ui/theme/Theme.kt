package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = RazorBlue,
    secondary = InstaPink,
    tertiary = RazorTeal,
    background = SolidObsidianDark,
    surface = SolidCardBackground,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = SolidLightText,
    onSurface = SolidLightText,
    surfaceVariant = MinimalSurfaceContainer,
    onSurfaceVariant = SolidGrayText,
    outline = MinimalBorder
)

private val LightColorScheme = lightColorScheme(
    primary = RazorBlue,
    secondary = InstaPink,
    tertiary = RazorTeal,
    background = Color(0xFFF8FAFC),      // Tailwind Slate-50 background
    surface = Color(0xFFFFFFFF),         // Tailwind White surface card
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF0F172A),    // Tailwind Slate-900 text
    onSurface = Color(0xFF0F172A),       // Tailwind Slate-900 surface text
    surfaceVariant = Color(0xFFF1F5F9),  // Tailwind Slate-100 container/pill
    onSurfaceVariant = Color(0xFF475569),// Tailwind Slate-600 secondary label
    outline = Color(0xFFE2E8F0)          // Tailwind Slate-200 border outline
)

@Composable
fun SocialHubTheme(
    darkTheme: Boolean = true, // Force the brilliant Dark space layout shown in the PDF
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
