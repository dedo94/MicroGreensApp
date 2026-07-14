package com.dedo94.microgreensapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.dedo94.microgreensapp.core.database.entity.TrayStepStatus
import com.dedo94.microgreensapp.ui.theme.Spacing

/** Badge colorato per lo stato di uno step pianificato (Fatto/Saltato); niente per PENDING. */
@Composable
fun StepStatusBadge(status: TrayStepStatus, modifier: Modifier = Modifier) {
    val (label, containerColor, onContainerColor) = when (status) {
        TrayStepStatus.DONE -> Triple(
            "Fatto",
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
        )
        TrayStepStatus.SKIPPED -> Triple(
            "Saltato",
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
        )
        TrayStepStatus.PENDING -> return
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(containerColor)
            .padding(horizontal = Spacing.xs, vertical = 2.dp),
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = onContainerColor)
    }
}
