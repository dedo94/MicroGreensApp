package com.dedo94.microgreensapp.ui.theme

import androidx.compose.ui.graphics.Color

// Schema Material 3 completo, derivato da 5 colori seed ("fresh & organic":
// verde foglia, verde salvia, accento sole, superficie chiarissima/verdastra
// in light, verde scurissimo in dark). onPrimary/onSecondary/onTertiary
// usano inchiostro scuro invece che bianco: i seed hanno una luminosità
// media che con testo bianco sopra fallisce il contrasto minimo WCAG AA
// (es. bianco su #4C956C = 3.61:1, sotto il minimo 4.5:1); con inchiostro
// scuro si resta sopra il 4.5:1 mantenendo i seed invariati.

// Light
val LightPrimary = Color(0xFF4C956C)
val LightOnPrimary = Color(0xFF121B16)
val LightPrimaryContainer = Color(0xFFE1EAE5)
val LightOnPrimaryContainer = Color(0xFF19251E)
val LightSecondary = Color(0xFF7A9E7E)
val LightOnSecondary = Color(0xFF1C211C)
val LightSecondaryContainer = Color(0xFFE3E8E4)
val LightOnSecondaryContainer = Color(0xFF1C211C)
val LightTertiary = Color(0xFFE8A33D)
val LightOnTertiary = Color(0xFF2D2110)
val LightTertiaryContainer = Color(0xFFF1E8DA)
val LightOnTertiaryContainer = Color(0xFF2D2110)
val LightError = Color(0xFFBA1A1A)
val LightOnError = Color(0xFFFFFFFF)
val LightErrorContainer = Color(0xFFF0DBDB)
val LightOnErrorContainer = Color(0xFF2C1111)
val LightBackground = Color(0xFFF6FBF6)
val LightOnBackground = Color(0xFF1B221E)
val LightSurface = Color(0xFFF6FBF6)
val LightOnSurface = Color(0xFF1B221E)
val LightSurfaceVariant = Color(0xFFE1EAE5)
val LightOnSurfaceVariant = Color(0xFF416350)
val LightOutline = Color(0xFF69967D)
val LightOutlineVariant = Color(0xFFCAD8D0)
val LightSurfaceDim = Color(0xFFDBE1DD)
val LightSurfaceBright = Color(0xFFF8F9F9)
val LightSurfaceContainerLowest = Color(0xFFFFFFFF)
val LightSurfaceContainerLow = Color(0xFFF4F6F5)
val LightSurfaceContainer = Color(0xFFEEF1F0)
val LightSurfaceContainerHigh = Color(0xFFE9EDEA)
val LightSurfaceContainerHighest = Color(0xFFE3E8E5)
val LightInverseSurface = Color(0xFF2C3A32)
val LightInverseOnSurface = Color(0xFFF1F3F2)
val LightInversePrimary = Color(0xFFB4D0C0)

// Dark
val DarkPrimary = Color(0xFFB4D0C0)
val DarkOnPrimary = Color(0xFF223028)
val DarkPrimaryContainer = Color(0xFF365945)
val DarkOnPrimaryContainer = Color(0xFFE3E8E5)
val DarkSecondary = Color(0xFFBBC9BD)
val DarkOnSecondary = Color(0xFF252C26)
val DarkSecondaryContainer = Color(0xFF3F5041)
val DarkOnSecondaryContainer = Color(0xFFE4E7E4)
val DarkTertiary = Color(0xFFE4C8A0)
val DarkOnTertiary = Color(0xFF3A2C17)
val DarkTertiaryContainer = Color(0xFF72501D)
val DarkOnTertiaryContainer = Color(0xFFEDE7DE)
val DarkError = Color(0xFFE2A1A1)
val DarkOnError = Color(0xFF3A1818)
val DarkErrorContainer = Color(0xFF701F1F)
val DarkOnErrorContainer = Color(0xFFECDFDF)
val DarkBackground = Color(0xFF0F1D14)
val DarkOnBackground = Color(0xFFE3E8E5)
val DarkSurface = Color(0xFF0F1D14)
val DarkOnSurface = Color(0xFFE3E8E5)
val DarkSurfaceVariant = Color(0xFF40594B)
val DarkOnSurfaceVariant = Color(0xFFC2D6CB)
val DarkOutline = Color(0xFF87AB97)
val DarkOutlineVariant = Color(0xFF40594B)
val DarkSurfaceDim = Color(0xFF0F1D14)
val DarkSurfaceBright = Color(0xFF45544C)
val DarkSurfaceContainerLowest = Color(0xFF0B0E0D)
val DarkSurfaceContainerLow = Color(0xFF1C221E)
val DarkSurfaceContainer = Color(0xFF222A26)
val DarkSurfaceContainerHigh = Color(0xFF2E3832)
val DarkSurfaceContainerHighest = Color(0xFF39463F)
val DarkInverseSurface = Color(0xFFE3E8E5)
val DarkInverseOnSurface = Color(0xFF2D3932)
val DarkInversePrimary = Color(0xFF4C956C)

val Scrim = Color(0xFF000000)

// Colore identificativo per varietà (pallino in lista/calendario): stesso
// colore per tutti i vassoi della stessa varietà. Tonalità attenuate
// (S~50%, L~56%) per restare in armonia con le superfici verdi dominanti
// invece di stonare come tinte pure/sature.
val TrayPalette = listOf(
    Color(0xFFC77357), // terracotta
    Color(0xFFC7C757), // oliva-oro
    Color(0xFF73C757), // verde prato
    Color(0xFF57C78F), // menta
    Color(0xFF57ABC7), // azzurro cielo
    Color(0xFF5757C7), // indaco
    Color(0xFFAB57C7), // viola
    Color(0xFFC7578F), // rosa
)
