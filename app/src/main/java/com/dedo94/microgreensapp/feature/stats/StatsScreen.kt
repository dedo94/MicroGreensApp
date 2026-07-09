package com.dedo94.microgreensapp.feature.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dedo94.microgreensapp.core.repository.StatsOverview
import com.dedo94.microgreensapp.core.repository.TrayStats
import com.dedo94.microgreensapp.core.repository.VarietyStats
import com.dedo94.microgreensapp.ui.displayLabel
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: StatsViewModel = hiltViewModel()) {
    val overview by viewModel.overview.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Statistiche") }) },
    ) { padding ->
        val currentOverview = overview
        if (currentOverview == null || currentOverview.trayStats.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("Crea e coltiva un vassoio per vedere le statistiche qui.")
            }
        } else {
            StatsContent(overview = currentOverview, modifier = Modifier.padding(padding))
        }
    }
}

@Composable
private fun StatsContent(overview: StatsOverview, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
    ) {
        if (overview.bestYieldTray != null || overview.bestYieldRatioTray != null || overview.shortestCycleTray != null) {
            item { SectionTitle("Record personali") }
            item {
                Column {
                    overview.bestYieldTray?.let {
                        RecordRow("Raccolto più abbondante", "${it.tray.name} · ${formatGrams(it.harvestTotalGrams)}")
                    }
                    overview.bestYieldRatioTray?.let {
                        RecordRow("Miglior resa per grammo di seme", "${it.tray.name} · ${formatRatio(it.yieldPerSeedGram)}")
                    }
                    overview.shortestCycleTray?.let {
                        RecordRow("Ciclo più breve", "${it.tray.name} · ${formatDays(it.actualCycleDays)}")
                    }
                }
            }
            item { Spacer(Modifier.height(12.dp)) }
        }

        if (overview.monthlyHarvestGrams.isNotEmpty()) {
            item { SectionTitle("Produzione mensile") }
            item {
                Column {
                    overview.monthlyHarvestGrams.reversed().forEach { (month, grams) ->
                        RecordRow(month.displayLabel(), formatGrams(grams))
                    }
                }
            }
            item { Spacer(Modifier.height(12.dp)) }
        }

        if (overview.varietyStats.isNotEmpty()) {
            item { SectionTitle("Per varietà") }
            items(items = overview.varietyStats, key = { it.varietyName }) { stats ->
                VarietyStatsCard(stats)
            }
            item { Spacer(Modifier.height(12.dp)) }
        }

        item { SectionTitle("Per vassoio") }
        items(items = overview.trayStats, key = { it.tray.id }) { stats ->
            TrayStatsCard(stats)
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
    )
}

@Composable
private fun RecordRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun VarietyStatsCard(stats: VarietyStats) {
    Card(modifier = Modifier.padding(vertical = 4.dp)) {
        Column(Modifier.padding(12.dp)) {
            Text(stats.varietyName, style = MaterialTheme.typography.titleSmall)
            val successInfo = if (stats.harvestedCount + stats.abandonedCount > 0) {
                val rate = 100.0 * stats.harvestedCount / (stats.harvestedCount + stats.abandonedCount)
                " · Successo: ${"%.0f".format(rate)}%"
            } else ""
            Text(
                text = "${stats.cycleCount} cicli$successInfo",
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = "Resa media: ${formatGrams(stats.avgHarvestGrams)} · Durata media: ${formatDays(stats.avgCycleDays?.toLong())}",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun TrayStatsCard(stats: TrayStats) {
    Card(modifier = Modifier.padding(vertical = 4.dp)) {
        Column(Modifier.padding(12.dp)) {
            Text(
                text = "${stats.tray.name} · ${stats.tray.varietyName} · ${stats.tray.status.displayLabel()}",
                style = MaterialTheme.typography.titleSmall,
            )
            val seedInfo = stats.tray.initialSeedQuantityGrams?.let { formatGrams(it) } ?: "—"
            Text(
                text = "Semi: $seedInfo · Acqua: ${"%.0f".format(stats.waterTotalMl)}ml · Raccolto: ${formatGrams(stats.harvestTotalGrams)}",
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = "Resa/seme: ${formatRatio(stats.yieldPerSeedGram)} · Acqua/g raccolto: ${formatRatio(stats.waterPerHarvestGram)}",
                style = MaterialTheme.typography.bodySmall,
            )
            val durationInfo = "Durata: ${formatDays(stats.actualCycleDays)}" +
                (stats.plannedCycleDays?.let { " (pianificata: ${formatDays(it)})" } ?: "")
            Text(text = durationInfo, style = MaterialTheme.typography.bodySmall)
            if (stats.avgTemperature != null || stats.avgHumidity != null) {
                val tempInfo = stats.avgTemperature?.let { "${"%.1f".format(it)}°C" } ?: "—"
                val humidityInfo = stats.avgHumidity?.let { "${"%.0f".format(it)}%" } ?: "—"
                Text(
                    text = "Condizioni medie: $tempInfo · $humidityInfo umidità",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

private fun YearMonth.displayLabel(): String {
    val monthName = month.getDisplayName(TextStyle.FULL, Locale.ITALIAN)
        .replaceFirstChar { it.uppercase() }
    return "$monthName $year"
}

private fun formatGrams(value: Double?): String = value?.let { "${"%.1f".format(it)}g" } ?: "—"

private fun formatRatio(value: Double?): String = value?.let { "%.2f".format(it) } ?: "—"

private fun formatDays(value: Long?): String = value?.let { "$it giorni" } ?: "—"
