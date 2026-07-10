package com.dedo94.microgreensapp.ui.charts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Barre semplici disegnate con altezze in Dp proporzionali al massimo,
 * invece che con Canvas/fillMaxHeight(fraction): niente vincoli di
 * dimensionamento da un genitore con altezza non fissa da propagare.
 */
@Composable
fun MonthlyBarChart(points: List<Pair<String, Double>>, modifier: Modifier = Modifier) {
    if (points.isEmpty()) return
    val maxValue = points.maxOf { it.second }.coerceAtLeast(0.0001)
    val maxBarHeight = 100.dp

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom,
    ) {
        points.forEach { (label, value) ->
            val barHeight = maxBarHeight * (value / maxValue).toFloat().coerceIn(0.03f, 1f)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${"%.0f".format(value)}g", style = MaterialTheme.typography.labelSmall)
                Spacer(Modifier.height(4.dp))
                Column(
                    modifier = Modifier
                        .width(24.dp)
                        .height(maxBarHeight),
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    Spacer(
                        modifier = Modifier
                            .width(24.dp)
                            .height(barHeight)
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp)),
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(label, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
