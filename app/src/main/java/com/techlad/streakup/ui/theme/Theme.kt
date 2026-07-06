package com.techlad.streakup.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = PrimaryTeal,
    onPrimary = BackgroundWarm,
    primaryContainer = SurfaceVariantLight,
    onPrimaryContainer = InkText,
    secondary = StreakTeal,
    onSecondary = PrimaryTeal,
    secondaryContainer = StreakTeal.copy(alpha = 0.2f),
    onSecondaryContainer = PrimaryTeal,
    tertiary = FlameAmber,
    onTertiary = InkText,
    tertiaryContainer = FlameAmber.copy(alpha = 0.2f),
    onTertiaryContainer = InkText,
    background = BackgroundWarm,
    onBackground = InkText,
    surface = SurfaceLight,
    onSurface = InkText,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    error = MissedDay,
    onError = BackgroundWarm,
    errorContainer = MissedDay.copy(alpha = 0.15f),
    onErrorContainer = MissedDay,
    outline = OnSurfaceVariantLight.copy(alpha = 0.5f),
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryTeal,
    onPrimary = OnSurfaceDark,
    primaryContainer = SurfaceVariantDark,
    onPrimaryContainer = OnSurfaceDark,
    secondary = StreakTeal,
    onSecondary = PrimaryTeal,
    secondaryContainer = StreakTeal.copy(alpha = 0.25f),
    onSecondaryContainer = StreakTeal,
    tertiary = FlameAmber,
    onTertiary = InkText,
    tertiaryContainer = FlameAmber.copy(alpha = 0.25f),
    onTertiaryContainer = FlameAmber,
    background = BackgroundDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    error = MissedDay,
    onError = OnSurfaceDark,
    errorContainer = MissedDay.copy(alpha = 0.25f),
    onErrorContainer = MissedDay,
    outline = OnSurfaceVariantDark.copy(alpha = 0.5f),
)

@Composable
fun StreakUpTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = {
            CompositionLocalProvider(
                LocalStreakUpColors provides StreakUpExtendedColors(),
            ) {
                content()
            }
        },
    )
}
