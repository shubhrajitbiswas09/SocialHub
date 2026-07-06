package com.example.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme

// --- High-Fidelity Apple Liquid Glass & Pink-Orange Theme - SocialHub Palette ---
// Refactored to feature a gorgeous, premium liquid glassmorphism Apple style:
// deep velvet galactic space background combined with neon tangerine and glowing liquid pink.

// Solid base static colors used for theme configurations
val SolidObsidianDark = Color(0xFF0E0B19)      // Majestic deep space matte cosmic canvas
val SolidCardBackground = Color(0x1AFFFFFF)    // Ultra-premium frosted glass background
val SolidLightText = Color(0xFFFDFCFF)         // Pristine white high-contrast text label
val SolidGrayText = Color(0xFFC7BFCF)          // Translucent metallic lavender-slate secondary text

// Reactive, dynamic theme-aware properties for standard styling across all core components
val ObsidianDark: Color
    @Composable
    get() = MaterialTheme.colorScheme.background

val CardBackground: Color
    @Composable
    get() = MaterialTheme.colorScheme.surface

val LightText: Color
    @Composable
    get() = MaterialTheme.colorScheme.onBackground

val GrayText: Color
    @Composable
    get() = MaterialTheme.colorScheme.onSurfaceVariant

// Glowing Brand Core and Accent Colors representing Apple liquid aesthetic
val RazorBlue = Color(0xFFFF7B40)         // Sleek liquid neon tangerine orange
val InstaPink = Color(0xFFFF4CA0)         // Glorious energetic liquid pink accent
val InstaOrange = Color(0xFFFF8B5E)       // Glowing bright peach orange satin hue
val RazorTeal = Color(0xFF4ADE80)         // Vibrant emerald green for safe badges / transactions
val SafeGold = Color(0xFFFBBF24)          // Brilliant liquid Amber gold

// Theme utilities for structural integration
val MinimalBg = Color(0xFF0E0B19)
val MinimalSurface = Color(0x26FFFFFF)     // High gloss translucent glass surface
val MinimalBorder = Color(0x33FF8BB5)     // Refined ultra-thin translucent liquid pink glass border glow
val MinimalPrimaryPurple = Color(0xFFFF4CA0)
val MinimalLavenderContainer = Color(0x3BFF4CA0) // Gorgeous liquid pink soft background container chips
val MinimalDeepPurple = Color(0xFF06040C)   // Deep solid shadow background
val MinimalLightPurple = Color(0xFFFFD5EC)  // Delicious baby liquid pastel pink label accent
val MinimalDarkPurple = Color(0xFF0E0B19)
val MinimalPurpleContainerLow = Color(0x14FF4CA0)
val MinimalSurfaceContainer = Color(0xF20E0B19) // Ultra-frosted navigation and system bar background

// compatibility
val RazorBlue80 = Color(0xFFFF8B5E)
val InstaPink80 = Color(0xFFFF7FB9)
val AccentTeal = Color(0xFF86EFAC)
