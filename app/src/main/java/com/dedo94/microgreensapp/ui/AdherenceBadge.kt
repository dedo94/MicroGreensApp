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
import com.dedo94.microgreensapp.ui.theme.Spacing

/** Badge colorato per la % di step del piano fatti come previsto (verde/ambra/rosso). */
@Composable
fun AdherenceBadge(percent: Double) {
    val containerColor = when {
        percent >= 80 -> MaterialTheme.colorScheme.primaryContainer
        percent >= 50 -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.errorContainer
    }
    val onContainerColor = when {
        percent >= 80 -> MaterialTheme.colorScheme.onPrimaryContainer
        percent >= 50 -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.onErrorContainer
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(containerColor)
            .padding(horizontal = Spacing.xs, vertical = 2.dp),
    ) {
        Text(
            text = "${percent.toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            color = onContainerColor,
        )
    }
}
