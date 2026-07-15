package com.dedo94.microgreensapp.ui.charts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class ProductionChartPoint(
    val label: String,
    val harvestGrams: Double,
    val seedGrams: Double,
)

/**
 * Due barre affiancate per periodo (raccolto e semi, stessa scala in
 * grammi), disegnate con altezze in Dp proporzionali al massimo tra
 * entrambe le serie: la differenza di scala (i semi sono in genere una
 * piccola frazione del raccolto) è parte del punto del grafico.
 */
@Composable
fun ProductionBarChart(points: List<ProductionChartPoint>, modifier: Modifier = Modifier) {
    if (points.isEmpty()) return
    val maxValue = points.maxOf { maxOf(it.harvestGrams, it.seedGrams) }.coerceAtLeast(0.0001)
    val maxBarHeight = 100.dp
    val harvestColor = MaterialTheme.colorScheme.primary
    val seedColor = MaterialTheme.colorScheme.tertiary

    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LegendDot(harvestColor)
            Spacer(Modifier.width(4.dp))
            Text("Raccolto", style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.width(12.dp))
            LegendDot(seedColor)
            Spacer(Modifier.width(4.dp))
            Text("Semi", style = MaterialTheme.typography.labelSmall)
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom,
        ) {
            points.forEach { point ->
                val harvestHeight = maxBarHeight * (point.harvestGrams / maxValue).toFloat().coerceIn(0f, 1f)
                val seedHeight = maxBarHeight * (point.seedGrams / maxValue).toFloat().coerceIn(0f, 1f)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier.height(maxBarHeight),
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        Bar(height = harvestHeight, color = harvestColor)
                        Bar(height = seedHeight, color = seedColor)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(point.label, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun Bar(height: Dp, color: Color) {
    Spacer(
        modifier = Modifier
            .width(14.dp)
            .height(height)
            .background(color, RoundedCornerShape(6.dp)),
    )
}

@Composable
private fun LegendDot(color: Color) {
    Spacer(
        modifier = Modifier
            .size(8.dp)
            .background(color, CircleShape),
    )
}
