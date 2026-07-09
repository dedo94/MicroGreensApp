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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dedo94.microgreensapp.core.repository.StatsOverview
import com.dedo94.microgreensapp.core.repository.TrayStats
import com.dedo94.microgreensapp.core.repository.VarietyStats
import com.dedo94.microgreensapp.ui.charts.MonthlyBarChart
import com.dedo94.microgreensapp.ui.charts.TrendLineChart
import com.dedo94.microgreensapp.ui.displayLabel
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: StatsViewModel = hiltViewModel()) {
    val overview by viewModel.overview.collectAsStateWithLifecycle()
    val varietyFilter by viewModel.varietyFilter.collectAsStateWithLifecycle()
    val compareTrayAId by viewModel.compareTrayAId.collectAsStateWithLifecycle()
    val compareTrayBId by viewModel.compareTrayBId.collectAsStateWithLifecycle()

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
            StatsContent(
                overview = currentOverview,
                varietyFilter = varietyFilter,
                onVarietyFilterChange = viewModel::onVarietyFilterChange,
                compareTrayAId = compareTrayAId,
                compareTrayBId = compareTrayBId,
                onCompareTrayAChange = viewModel::onCompareTrayAChange,
                onCompareTrayBChange = viewModel::onCompareTrayBChange,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatsContent(
    overview: StatsOverview,
    varietyFilter: String?,
    onVarietyFilterChange: (String?) -> Unit,
    compareTrayAId: Long?,
    compareTrayBId: Long?,
    onCompareTrayAChange: (Long?) -> Unit,
    onCompareTrayBChange: (Long?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val filteredTrayStats = remember(overview, varietyFilter) {
        if (varietyFilter == null) overview.trayStats else overview.trayStats.filter { it.tray.varietyName == varietyFilter }
    }
    val filteredVarietyStats = remember(overview, varietyFilter) {
        if (varietyFilter == null) overview.varietyStats else overview.varietyStats.filter { it.varietyName == varietyFilter }
    }
    val trendPoints = remember(overview, varietyFilter) {
        if (varietyFilter == null) {
            emptyList()
        } else {
            overview.trayStats
                .filter { it.tray.varietyName == varietyFilter && it.harvestTotalGrams != null }
                .sortedBy { it.tray.sowingDate }
                .map { it.tray.sowingDate.format(shortDateFormatter) to it.harvestTotalGrams!! }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
    ) {
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = varietyFilter == null,
                        onClick = { onVarietyFilterChange(null) },
                        label = { Text("Tutte le varietà") },
                    )
                }
                items(overview.varietyStats, key = { it.varietyName }) { stats ->
                    FilterChip(
                        selected = varietyFilter == stats.varietyName,
                        onClick = { onVarietyFilterChange(stats.varietyName) },
                        label = { Text(stats.varietyName) },
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
        }

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
                MonthlyBarChart(
                    points = overview.monthlyHarvestGrams.map { (month, grams) -> month.shortLabel() to grams },
                )
            }
            item { Spacer(Modifier.height(12.dp)) }
        }

        if (trendPoints.isNotEmpty()) {
            item { SectionTitle("Andamento resa · $varietyFilter") }
            item { TrendLineChart(points = trendPoints) }
            item { Spacer(Modifier.height(12.dp)) }
        }

        if (filteredVarietyStats.isNotEmpty()) {
            item { SectionTitle("Per varietà") }
            items(items = filteredVarietyStats, key = { it.varietyName }) { stats ->
                VarietyStatsCard(stats)
            }
            item { Spacer(Modifier.height(12.dp)) }
        }

        item { SectionTitle("Per vassoio") }
        items(items = filteredTrayStats, key = { it.tray.id }) { stats ->
            TrayStatsCard(stats)
        }

        item { Spacer(Modifier.height(12.dp)) }
        item { SectionTitle("Confronta due vassoi") }
        item {
            CompareSection(
                trayStats = overview.trayStats,
                trayAId = compareTrayAId,
                trayBId = compareTrayBId,
                onTrayAChange = onCompareTrayAChange,
                onTrayBChange = onCompareTrayBChange,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompareSection(
    trayStats: List<TrayStats>,
    trayAId: Long?,
    trayBId: Long?,
    onTrayAChange: (Long?) -> Unit,
    onTrayBChange: (Long?) -> Unit,
) {
    val trayA = trayStats.find { it.tray.id == trayAId }
    val trayB = trayStats.find { it.tray.id == trayBId }

    Row(Modifier.fillMaxWidth()) {
        TrayPickerDropdown(
            label = "Vassoio A",
            trayStats = trayStats,
            selectedTrayId = trayAId,
            onSelect = onTrayAChange,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(8.dp))
        TrayPickerDropdown(
            label = "Vassoio B",
            trayStats = trayStats,
            selectedTrayId = trayBId,
            onSelect = onTrayBChange,
            modifier = Modifier.weight(1f),
        )
    }

    if (trayA != null && trayB != null) {
        Spacer(Modifier.height(8.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("", modifier = Modifier.weight(1f))
                    Text(trayA.tray.name, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                    Text(trayB.tray.name, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                ComparisonRow("Semi", formatGrams(trayA.tray.initialSeedQuantityGrams), formatGrams(trayB.tray.initialSeedQuantityGrams))
                ComparisonRow("Acqua", "${"%.0f".format(trayA.waterTotalMl)}ml", "${"%.0f".format(trayB.waterTotalMl)}ml")
                ComparisonRow("Raccolto", formatGrams(trayA.harvestTotalGrams), formatGrams(trayB.harvestTotalGrams))
                ComparisonRow("Resa/seme", formatRatio(trayA.yieldPerSeedGram), formatRatio(trayB.yieldPerSeedGram))
                ComparisonRow("Efficienza idrica", formatRatio(trayA.waterPerHarvestGram), formatRatio(trayB.waterPerHarvestGram))
                ComparisonRow("Durata", formatDays(trayA.actualCycleDays), formatDays(trayB.actualCycleDays))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TrayPickerDropdown(
    label: String,
    trayStats: List<TrayStats>,
    selectedTrayId: Long?,
    onSelect: (Long?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = trayStats.find { it.tray.id == selectedTrayId }?.tray?.name ?: "Scegli"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            trayStats.forEach { stats ->
                DropdownMenuItem(
                    text = { Text(stats.tray.name) },
                    onClick = {
                        onSelect(stats.tray.id)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun ComparisonRow(label: String, valueA: String, valueB: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        Text(valueA, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        Text(valueB, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
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

private val shortDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM")

private fun YearMonth.shortLabel(): String {
    val monthName = month.getDisplayName(TextStyle.SHORT, Locale.ITALIAN)
    return "$monthName '${(year % 100).toString().padStart(2, '0')}"
}

private fun formatGrams(value: Double?): String = value?.let { "${"%.1f".format(it)}g" } ?: "—"

private fun formatRatio(value: Double?): String = value?.let { "%.2f".format(it) } ?: "—"

private fun formatDays(value: Long?): String = value?.let { "$it giorni" } ?: "—"
