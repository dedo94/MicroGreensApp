package com.dedo94.microgreensapp.ui.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp

/**
 * Linea che collega i punti nell'ordine in cui sono passati (asse X non
 * quantitativo, solo ordine cronologico), con un'etichetta breve sotto
 * ogni punto. Disegnata con Canvas: nessuna libreria di charting aggiunta,
 * sufficiente per un andamento a pochi punti come i cicli di una varietà.
 */
@Composable
fun TrendLineChart(points: List<Pair<String, Double>>, modifier: Modifier = Modifier) {
    if (points.isEmpty()) return
    val maxValue = points.maxOf { it.second }
    val minValue = points.minOf { it.second }.coerceAtMost(0.0)
    val range = (maxValue - minValue).takeIf { it > 0 } ?: 1.0
    val lineColor = MaterialTheme.colorScheme.primary

    Column(modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .padding(vertical = 8.dp, horizontal = 4.dp),
        ) {
            val stepX = if (points.size > 1) size.width / (points.size - 1) else 0f
            val offsets = points.mapIndexed { index, (_, value) ->
                val x = stepX * index
                val y = size.height - ((value - minValue) / range).toFloat() * size.height
                Offset(x, y)
            }
            for (i in 0 until offsets.size - 1) {
                drawLine(color = lineColor, start = offsets[i], end = offsets[i + 1], strokeWidth = 4f)
            }
            offsets.forEach { offset -> drawCircle(color = lineColor, radius = 6f, center = offset) }
        }
        if (points.size > 1) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(points.first().first, style = MaterialTheme.typography.labelSmall)
                Text(points.last().first, style = MaterialTheme.typography.labelSmall)
            }
        } else {
            Text(points.first().first, style = MaterialTheme.typography.labelSmall)
        }
    }
}
