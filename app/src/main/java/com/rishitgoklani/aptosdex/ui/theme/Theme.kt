package com.rishitgoklani.aptosdex.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AptosBlue,
    onPrimary = TextPrimary,
    primaryContainer = Color(0xFF113F4B),
    onPrimaryContainer = AptosBlue,

    secondary = SoftPurple,
    onSecondary = TextPrimary,
    secondaryContainer = Color(0xFF2A1F3A),
    onSecondaryContainer = SoftPurple,

    background = SurfaceBlack,
    onBackground = TextPrimary,

    surface = SurfaceBlack,
    onSurface = TextPrimary,
    surfaceVariant = Color(0xFF1F2430),
    onSurfaceVariant = TextSecondary,

    outline = OutlineBlue,
    error = ErrorRed,
    onError = TextPrimary,
    errorContainer = Color(0xFF3D1216),
    onErrorContainer = ErrorRed,

    tertiary = SuccessGreen,
    onTertiary = TextPrimary,
    tertiaryContainer = Color(0xFF12381A),
    onTertiaryContainer = SuccessGreen
)

private val LightColorScheme = lightColorScheme(
    primary = AptosBlue,
    onPrimary = Color(0xFF00151A),
    primaryContainer = Color(0xFFB4ECF8),
    onPrimaryContainer = Color(0xFF00232B),

    secondary = SoftPurple,
    onSecondary = Color(0xFF1E0F2A),
    secondaryContainer = Color(0xFFE7D6FF),
    onSecondaryContainer = Color(0xFF2A1F3A),

    background = Color(0xFFF7F9FB),
    onBackground = Color(0xFF0E1216),

    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0E1216),
    surfaceVariant = Color(0xFFE3E8EE),
    onSurfaceVariant = Color(0xFF495563),

    outline = Color(0x332A6F80),
    error = ErrorRed,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    tertiary = SuccessGreen,
    onTertiary = Color(0xFF07210F),
    tertiaryContainer = Color(0xFFC0F1C7),
    onTertiaryContainer = Color(0xFF12381A)
)

@Composable
fun AptosDEXTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
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