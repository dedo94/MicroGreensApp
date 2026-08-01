package com.dedo94.microgreensapp.ui.theme

import androidx.compose.ui.graphics.Color

// Schema Material 3 derivato dai design token del redesign v2 (mockup
// Claude Design, vedi handoff fornito dall'utente). I nomi dei ruoli
// ColorScheme non coincidono 1:1 con i token del mockup (--surface-low,
// --card, --surface-container, ecc.): "card" (superficie delle Card, più
// chiara/elevata dello sfondo schermata in entrambi i temi) è mappato su
// surfaceContainerLow perché è il default di CardDefaults.cardColors() in
// Material3; "surface-low" (sfondo schermata, più "affondato") su
// background/surface/surfaceContainerLowest. Nessun token bare "secondary"
// nel mockup (solo secondary-container): secondary/onSecondary riusano
// primary/onPrimary, dato che nessuna schermata usa il ruolo "secondary" da
// solo, evitando di inventare un valore che fallirebbe il contrasto minimo.

// Light
val LightPrimary = Color(0xFF2E9E58)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFFDDF2E2)
val LightOnPrimaryContainer = Color(0xFF0F3A20)
val LightSecondary = LightPrimary
val LightOnSecondary = LightOnPrimary
val LightSecondaryContainer = Color(0xFFE4F1E7)
val LightOnSecondaryContainer = Color(0xFF1C3324)
val LightTertiary = Color(0xFFE8A33D)
val LightOnTertiary = Color(0xFF2D2110)
val LightTertiaryContainer = Color(0xFFF1E8DA)
val LightOnTertiaryContainer = Color(0xFF2D2110)
val LightError = Color(0xFFBA1A1A)
val LightOnError = Color(0xFFFFFFFF)
val LightErrorContainer = Color(0xFFF0DBDB)
val LightOnErrorContainer = Color(0xFF2C1111)
val LightSurfaceLow = Color(0xFFF3FAF5)
val LightBackground = LightSurfaceLow
val LightOnBackground = Color(0xFF12241A)
val LightSurface = LightSurfaceLow
val LightOnSurface = Color(0xFF12241A)
val LightSurfaceVariant = Color(0xFFCFE5D6)
val LightOnSurfaceVariant = Color(0xFF5C7869)
val LightOutline = Color(0xFF4E9268)
val LightOutlineVariant = Color(0xFFCFE5D6)
val LightSurfaceDim = Color(0xFFDCEEE1)
val LightSurfaceBright = Color(0xFFFFFFFF)
val LightCard = Color(0xFFFFFFFF)
val LightSurfaceContainerLowest = LightSurfaceLow
val LightSurfaceContainerLow = LightCard
val LightSurfaceContainer = Color(0xFFE9F5EC)
val LightSurfaceContainerHigh = Color(0xFFDCEEE1)
val LightSurfaceContainerHighest = LightSurfaceContainerHigh
val LightInverseSurface = Color(0xFF12241A)
val LightInverseOnSurface = Color(0xFFF3FAF5)
val LightInversePrimary = Color(0xFF4FCB80)

// Dark
val DarkPrimary = Color(0xFF4FCB80)
val DarkOnPrimary = Color(0xFF04170C)
val DarkPrimaryContainer = Color(0xFF123A22)
val DarkOnPrimaryContainer = Color(0xFFB9EFC9)
val DarkSecondary = DarkPrimary
val DarkOnSecondary = DarkOnPrimary
val DarkSecondaryContainer = Color(0xFF132A1A)
val DarkOnSecondaryContainer = Color(0xFFCFE6D6)
val DarkTertiary = Color(0xFFE4C8A0)
val DarkOnTertiary = Color(0xFF3A2C17)
val DarkTertiaryContainer = Color(0xFF4A3A20)
val DarkOnTertiaryContainer = Color(0xFFEDE7DE)
val DarkError = Color(0xFFE2A1A1)
val DarkOnError = Color(0xFF3A1414)
val DarkErrorContainer = Color(0xFF3A1414)
val DarkOnErrorContainer = Color(0xFFECDFDF)
val DarkSurfaceLow = Color(0xFF0C1610)
val DarkBackground = DarkSurfaceLow
val DarkOnBackground = Color(0xFFEAF4EC)
val DarkSurface = DarkSurfaceLow
val DarkOnSurface = Color(0xFFEAF4EC)
val DarkSurfaceVariant = Color(0xFF1D3324)
val DarkOnSurfaceVariant = Color(0xFF84A691)
val DarkOutline = Color(0xFF3C6E4C)
val DarkOutlineVariant = Color(0xFF1D3324)
val DarkSurfaceDim = Color(0xFF060D08)
val DarkSurfaceBright = Color(0xFF152417)
val DarkCard = Color(0xFF0D1810)
val DarkSurfaceContainerLowest = Color(0xFF060D08)
val DarkSurfaceContainerLow = DarkCard
val DarkSurfaceContainer = Color(0xFF0F1C13)
val DarkSurfaceContainerHigh = Color(0xFF152417)
val DarkSurfaceContainerHighest = DarkSurfaceContainerHigh
val DarkInverseSurface = Color(0xFFEAF4EC)
val DarkInverseOnSurface = Color(0xFF0C1610)
val DarkInversePrimary = Color(0xFF2E9E58)

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
