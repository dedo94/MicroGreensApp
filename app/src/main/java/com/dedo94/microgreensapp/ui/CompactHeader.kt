package com.dedo94.microgreensapp.ui

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.dedo94.microgreensapp.ui.theme.Spacing

/**
 * Intestazione compatta al posto di una TopAppBar (64dp): stesso titolo,
 * circa metà dell'altezza. onBack/actions opzionali per le schermate
 * raggiunte "andando avanti" che prima usavano una TopAppBar completa.
 */
@Composable
fun CompactHeader(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(
            start = if (onBack != null) Spacing.xs else Spacing.md,
            end = Spacing.xs,
        ),
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Indietro")
            }
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(vertical = Spacing.sm)
                .weight(1f),
        )
        actions()
    }
}
