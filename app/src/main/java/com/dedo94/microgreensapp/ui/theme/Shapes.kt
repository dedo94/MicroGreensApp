package com.dedo94.microgreensapp.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Corner radius più generosi dei default Material 3, applicati a tutti i
 * componenti (Card, Button, TextField, AlertDialog, ecc.) tramite
 * MaterialTheme: basta questo, senza dover toccare ogni composable.
 */
val MicroGreensShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)
