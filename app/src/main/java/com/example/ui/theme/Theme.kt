package com.example.ui.theme

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
    primary = OverComerPrimaryDark,
    secondary = OverComerSecondaryDark,
    tertiary = OverComerTertiaryDark,
    background = OverComerBackgroundDark,
    surface = OverComerSurfaceDark,
    surfaceVariant = OverComerSurfaceVariantDark,
    primaryContainer = Color(0xFF381E72),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondaryContainer = Color(0xFF2D2A33),
    onSecondaryContainer = Color(0xFFE6E1E5),
    onPrimary = Color(0xFF21005D),      // Clean deep purple contrast
    onSecondary = Color(0xFF21005D),
    onTertiary = Color(0xFFFFFFFF),
    onBackground = OverComerOnBackgroundDark,
    onSurface = OverComerOnBackgroundDark,
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF49454F)
)

private val LightColorScheme = lightColorScheme(
    primary = OverComerPrimaryLight,
    secondary = OverComerSecondaryLight,
    tertiary = OverComerTertiaryLight,
    background = OverComerBackgroundLight,
    surface = OverComerSurfaceLight,
    surfaceVariant = OverComerSurfaceVariantLight,
    primaryContainer = Color(0xFFEADDFF),    // Matches #EADDFF in HTML (Greeting Card bg)
    onPrimaryContainer = Color(0xFF21005D),  // Matches #21005D in HTML (Greeting Card text)
    secondaryContainer = Color(0xFFF3EDF7),  // Matches #F3EDF7 in HTML (Component Card background)
    onSecondaryContainer = Color(0xFF21005D),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1D1B20),    // Charcoal black from HTML
    onSurface = Color(0xFF1D1B20),        // Charcoal black from HTML
    onSurfaceVariant = Color(0xFF49454F),   // Dark silver/gray from HTML
    outline = Color(0xFFCAC4D0)          // Silver boundary line
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Standard system settings template can default to our cohesive hand-designed palette
    dynamicColor: Boolean = false, 
    content: @Composable () -> Unit,
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
