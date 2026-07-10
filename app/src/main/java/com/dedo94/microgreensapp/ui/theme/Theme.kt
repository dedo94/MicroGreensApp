package com.dedo94.microgreensapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = GreenPrimary,
    secondary = SunYellow,
    tertiary = Terracotta,
    background = Background,
    surface = Surface,
)

private val DarkColors = darkColorScheme(
    primary = GreenPrimaryDark,
    secondary = SunYellowDark,
    tertiary = TerracottaDark,
    background = BackgroundDark,
    surface = SurfaceDark,
)

/**
 * Nessun dynamic color (Android 12+): userebbe i colori estratti dallo
 * sfondo del telefono al posto della palette verde/allegra pensata per
 * l'app, rendendo il tema invisibile sulla maggior parte dei dispositivi
 * moderni.
 */
@Composable
fun MicroGreensAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = MicroGreensTypography,
        shapes = MicroGreensShapes,
        content = content,
    )
}
