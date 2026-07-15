package com.dedo94.microgreensapp.feature.stats

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.QueryStats
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dedo94.microgreensapp.core.repository.StatsOverview
import com.dedo94.microgreensapp.core.repository.StatsSummary
import com.dedo94.microgreensapp.core.repository.TrayStats
import com.dedo94.microgreensapp.core.repository.VarietyStats
import com.dedo94.microgreensapp.ui.AdherenceBadge
import com.dedo94.microgreensapp.ui.CompactHeader
import com.dedo94.microgreensapp.ui.charts.ProductionBarChart
import com.dedo94.microgreensapp.ui.charts.ProductionChartPoint
import com.dedo94.microgreensapp.ui.charts.TrendLineChart
import com.dedo94.microgreensapp.ui.displayColor
import com.dedo94.microgreensapp.ui.displayLabel
import com.dedo94.microgreensapp.ui.theme.Spacing
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: StatsViewModel = hiltViewModel()) {
    val overview by viewModel.overview.collectAsStateWithLifecycle()
    val compareTrayAId by viewModel.compareTrayAId.collectAsStateWithLifecycle()
    val compareTrayBId by viewModel.compareTrayBId.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        CompactHeader("Statistiche")
        val currentOverview = overview
        if (currentOverview == null || currentOverview.trayStats.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Spacing.xl),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = Icons.Outlined.QueryStats,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(Spacing.md))
                Text(
                    text = "Crea e coltiva un vassoio per vedere le statistiche qui.",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        } else {
            StatsContent(
                overview = currentOverview,
                compareTrayAId = compareTrayAId,
                compareTrayBId = compareTrayBId,
                onCompareTrayAChange = viewModel::onCompareTrayAChange,
                onCompareTrayBChange = viewModel::onCompareTrayBChange,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatsContent(
    overview: StatsOverview,
    compareTrayAId: Long?,
    compareTrayBId: Long?,
    onCompareTrayAChange: (Long?) -> Unit,
    onCompareTrayBChange: (Long?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val varietyColor = remember(overview) {
        overview.trayStats.associate { it.tray.varietyName to it.tray.displayColor() }
    }
    // pagina 0 = "Tutte le varietà", pagine successive una per varietà: lo
    // swipe tra pagine sostituisce il filtro, non serve più tenerlo in un
    // MutableStateFlow separato nel ViewModel.
    val pagerState = rememberPagerState(pageCount = { 1 + overview.varietyStats.size })
    val coroutineScope = rememberCoroutineScope()
    // Condivisa tra tutte le pagine dello swipe, non per-varietà: se l'utente
    // passa a "Mese" se lo ritrova anche scorrendo su un'altra varietà.
    var showYearlyProduction by remember { mutableStateOf(true) }

    Column(modifier.fillMaxSize()) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md, vertical = Spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            item {
                FilterChip(
                    selected = pagerState.currentPage == 0,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } },
                    label = { Text("Tutte le varietà") },
                )
            }
            itemsIndexed(overview.varietyStats, key = { _, stats -> stats.varietyName }) { index, stats ->
                FilterChip(
                    selected = pagerState.currentPage == index + 1,
                    onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index + 1) } },
                    label = { Text(stats.varietyName) },
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) { page ->
            val varietyFilter = if (page == 0) null else overview.varietyStats.getOrNull(page - 1)?.varietyName
            StatsPageContent(
                overview = overview,
                varietyFilter = varietyFilter,
                varietyColor = varietyColor,
                compareTrayAId = compareTrayAId,
                compareTrayBId = compareTrayBId,
                onCompareTrayAChange = onCompareTrayAChange,
                onCompareTrayBChange = onCompareTrayBChange,
                showYearlyProduction = showYearlyProduction,
                onShowYearlyProductionChange = { showYearlyProduction = it },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatsPageContent(
    overview: StatsOverview,
    varietyFilter: String?,
    varietyColor: Map<String, Color>,
    compareTrayAId: Long?,
    compareTrayBId: Long?,
    onCompareTrayAChange: (Long?) -> Unit,
    onCompareTrayBChange: (Long?) -> Unit,
    showYearlyProduction: Boolean,
    onShowYearlyProductionChange: (Boolean) -> Unit,
) {
    val filteredTrayStats = remember(overview, varietyFilter) {
        if (varietyFilter == null) overview.trayStats else overview.trayStats.filter { it.tray.varietyName == varietyFilter }
    }
    val filteredVarietyStats = remember(overview, varietyFilter) {
        if (varietyFilter == null) overview.varietyStats else overview.varietyStats.filter { it.varietyName == varietyFilter }
    }
    val summary = remember(overview, varietyFilter) {
        if (varietyFilter == null) overview.overallSummary else overview.summaryByVariety[varietyFilter] ?: overview.overallSummary
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

    var expandedTrayIds by remember { mutableStateOf(emptySet<Long>()) }
    var compareSectionExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.md),
        contentPadding = PaddingValues(top = Spacing.sm, bottom = Spacing.md),
    ) {
        item { KpiHeroRow(summary) }

        if (summary.bestYieldTray != null || summary.bestYieldRatioTray != null || summary.shortestCycleTray != null) {
            item { SectionTitle("Record personali") }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(Spacing.sm)) {
                        summary.bestYieldTray?.let {
                            RecordRow("Raccolto più abbondante", "${it.tray.name} · ${formatGrams(it.harvestTotalGrams)}")
                        }
                        summary.bestYieldRatioTray?.let {
                            RecordRow("Miglior resa per grammo di seme", "${it.tray.name} · ${formatRatio(it.yieldPerSeedGram)}")
                        }
                        summary.shortestCycleTray?.let {
                            RecordRow("Ciclo più breve", "${it.tray.name} · ${formatDays(it.actualCycleDays)}")
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(Spacing.sm)) }
        }

        val productionPoints = if (showYearlyProduction) {
            summary.yearlyProduction.map { ProductionChartPoint(it.year.toString(), it.harvestGrams, it.seedGrams) }
        } else {
            summary.monthlyProduction.map { ProductionChartPoint(it.month.shortLabel(), it.harvestGrams, it.seedGrams) }
        }
        if (productionPoints.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Spacing.md, bottom = Spacing.sm),
                ) {
                    Text(
                        text = if (varietyFilter == null) "Produzione" else "Produzione · $varietyFilter",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.height(Spacing.xs))
                    SingleChoiceSegmentedButtonRow {
                        SegmentedButton(
                            selected = showYearlyProduction,
                            onClick = { onShowYearlyProductionChange(true) },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                            label = { Text("Anno") },
                        )
                        SegmentedButton(
                            selected = !showYearlyProduction,
                            onClick = { onShowYearlyProductionChange(false) },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                            label = { Text("Mese") },
                        )
                    }
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    ProductionBarChart(
                        points = productionPoints,
                        modifier = Modifier.padding(Spacing.sm),
                    )
                }
            }
            item { Spacer(Modifier.height(Spacing.sm)) }
        }

        if (trendPoints.isNotEmpty()) {
            item { SectionTitle("Andamento resa · $varietyFilter") }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    TrendLineChart(points = trendPoints, modifier = Modifier.padding(Spacing.sm))
                }
            }
            item { Spacer(Modifier.height(Spacing.sm)) }
        }

        if (filteredVarietyStats.isNotEmpty()) {
            item { SectionTitle("Confronto varietà") }
            item {
                VarietyComparisonCard(
                    stats = filteredVarietyStats,
                    varietyColor = varietyColor,
                )
            }
            item { Spacer(Modifier.height(Spacing.sm)) }
        }

        item { SectionTitle("Vassoi") }
        items(items = filteredTrayStats, key = { it.tray.id }) { stats ->
            TrayRow(
                stats = stats,
                expanded = stats.tray.id in expandedTrayIds,
                onToggle = {
                    expandedTrayIds = if (stats.tray.id in expandedTrayIds) {
                        expandedTrayIds - stats.tray.id
                    } else {
                        expandedTrayIds + stats.tray.id
                    }
                },
                modifier = Modifier.animateItem(),
            )
        }

        item { Spacer(Modifier.height(Spacing.sm)) }
        item {
            CompareToggleSection(
                expanded = compareSectionExpanded,
                onToggle = { compareSectionExpanded = !compareSectionExpanded },
                trayStats = overview.trayStats,
                trayAId = compareTrayAId,
                trayBId = compareTrayBId,
                onTrayAChange = onCompareTrayAChange,
                onTrayBChange = onCompareTrayBChange,
            )
        }
    }
}

@Composable
private fun KpiHeroRow(summary: StatsSummary) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        KpiTile(
            value = summary.activeTrayCount.toString(),
            label = "Vassoi attivi",
            modifier = Modifier.weight(1f),
        )
        KpiTile(
            value = formatGrams(summary.last30DaysHarvestGrams),
            label = "Raccolto (30gg)",
            modifier = Modifier.weight(1f),
        )
        KpiTile(
            value = formatRatio(summary.avgYieldPerSeedGram),
            label = "Resa media/seme",
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun KpiTile(value: String, label: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun VarietyComparisonCard(
    stats: List<VarietyStats>,
    varietyColor: Map<String, Color>,
) {
    val fallbackColor = MaterialTheme.colorScheme.outline
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(Spacing.sm)) {
            Row(Modifier.fillMaxWidth()) {
                Spacer(Modifier.size(12.dp))
                Text("", modifier = Modifier.weight(1.4f))
                Text("Resa media", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                Text("Durata media", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                Text("Resa/seme", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
            }
            stats.forEachIndexed { index, variety ->
                if (index > 0) HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.xs))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Spacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(varietyColor[variety.varietyName] ?: fallbackColor),
                    )
                    Spacer(Modifier.width(Spacing.xs))
                    Text(
                        text = variety.varietyName,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1.4f),
                    )
                    Text(formatGrams(variety.avgHarvestGrams), style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    Text(formatDays(variety.avgCycleDays?.toLong()), style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    Text(formatRatio(variety.avgYieldPerSeedGram), style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun TrayRow(
    stats: TrayStats,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.padding(vertical = Spacing.xs)) {
        Column(
            modifier = Modifier
                .clickable(onClick = onToggle)
                .animateContentSize(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(stats.tray.displayColor()),
                )
                Spacer(Modifier.width(Spacing.sm))
                Column(Modifier.weight(1f)) {
                    Text(stats.tray.name, style = MaterialTheme.typography.titleSmall)
                    Text(
                        text = "${stats.tray.varietyName} · ${stats.tray.status.displayLabel()} · semina ${stats.tray.sowingDate.format(shortDateFormatter)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                stats.adherencePercent?.let { AdherenceBadge(it) }
                Spacer(Modifier.width(Spacing.xs))
                Icon(
                    imageVector = if (expanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                    contentDescription = if (expanded) "Comprimi" else "Espandi",
                )
            }
            if (expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = Spacing.sm, end = Spacing.sm, bottom = Spacing.sm),
                ) {
                    HorizontalDivider(modifier = Modifier.padding(bottom = Spacing.sm))
                    val seedInfo = stats.tray.initialSeedQuantityGrams?.let { formatGrams(it) } ?: "—"
                    Text(
                        text = "Semi: $seedInfo · Acqua: ${formatMl(stats.waterTotalMl)} · Raccolto: ${formatGrams(stats.harvestTotalGrams)}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        text = "Resa/seme: ${formatRatio(stats.yieldPerSeedGram)} · Acqua/g raccolto: ${formatRatio(stats.waterPerHarvestGram)}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    val durationInfo = "Durata: ${formatDays(stats.actualCycleDays)}" +
                        (stats.plannedCycleDays?.let { " (pianificata: ${formatDays(it)})" } ?: "")
                    Text(text = durationInfo, style = MaterialTheme.typography.bodySmall)
                    val tempInfo = stats.avgTemperature?.let { "${"%.1f".format(it)}°C" } ?: "—"
                    val humidityInfo = stats.avgHumidity?.let { "${"%.0f".format(it)}%" } ?: "—"
                    Text(
                        text = "Condizioni medie: $tempInfo · $humidityInfo umidità",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    if (stats.adherencePercent != null) {
                        Text(
                            text = "Aderenza al piano: ${stats.stepsDone} fatti, ${stats.stepsSkipped} saltati",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompareToggleSection(
    expanded: Boolean,
    onToggle: () -> Unit,
    trayStats: List<TrayStats>,
    trayAId: Long?,
    trayBId: Long?,
    onTrayAChange: (Long?) -> Unit,
    onTrayBChange: (Long?) -> Unit,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(vertical = Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Confronta due vassoi", style = MaterialTheme.typography.titleMedium)
            Icon(
                imageVector = if (expanded) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
                contentDescription = if (expanded) "Comprimi" else "Espandi",
            )
        }
        Box(Modifier.animateContentSize()) {
            if (expanded) {
                CompareSection(
                    trayStats = trayStats,
                    trayAId = trayAId,
                    trayBId = trayBId,
                    onTrayAChange = onTrayAChange,
                    onTrayBChange = onTrayBChange,
                )
            }
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

    Column {
        Row(Modifier.fillMaxWidth()) {
            TrayPickerDropdown(
                label = "Vassoio A",
                trayStats = trayStats,
                selectedTrayId = trayAId,
                onSelect = onTrayAChange,
                modifier = Modifier.weight(1f),
            )
            Spacer(Modifier.width(Spacing.sm))
            TrayPickerDropdown(
                label = "Vassoio B",
                trayStats = trayStats,
                selectedTrayId = trayBId,
                onSelect = onTrayBChange,
                modifier = Modifier.weight(1f),
            )
        }

        Box(Modifier.animateContentSize()) {
            if (trayA != null && trayB != null) {
                Column {
                    Spacer(Modifier.height(Spacing.sm))
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(Spacing.sm)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("", modifier = Modifier.weight(1f))
                                Text(trayA.tray.name, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                                Text(trayB.tray.name, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.sm))
                            ComparisonRow("Semi", formatGrams(trayA.tray.initialSeedQuantityGrams), formatGrams(trayB.tray.initialSeedQuantityGrams))
                            ComparisonRow("Acqua", formatMl(trayA.waterTotalMl), formatMl(trayB.waterTotalMl))
                            ComparisonRow("Raccolto", formatGrams(trayA.harvestTotalGrams), formatGrams(trayB.harvestTotalGrams))
                            ComparisonRow("Resa/seme", formatRatio(trayA.yieldPerSeedGram), formatRatio(trayB.yieldPerSeedGram))
                            ComparisonRow("Efficienza idrica", formatRatio(trayA.waterPerHarvestGram), formatRatio(trayB.waterPerHarvestGram))
                            ComparisonRow("Durata", formatDays(trayA.actualCycleDays), formatDays(trayB.actualCycleDays))
                        }
                    }
                }
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
            .padding(vertical = Spacing.xs),
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
        modifier = Modifier.padding(top = Spacing.md, bottom = Spacing.sm),
    )
}

@Composable
private fun RecordRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

private val shortDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM")

private fun YearMonth.shortLabel(): String {
    val monthName = month.getDisplayName(TextStyle.SHORT, Locale.ITALIAN)
    return "$monthName '${(year % 100).toString().padStart(2, '0')}"
}

private fun formatGrams(value: Double?): String = value?.let { "${"%.1f".format(it)}g" } ?: "—"

private fun formatMl(value: Double?): String = value?.let { "${"%.0f".format(it)}ml" } ?: "—"

private fun formatRatio(value: Double?): String = value?.let { "%.2f".format(it) } ?: "—"

private fun formatDays(value: Long?): String = value?.let { "$it giorni" } ?: "—"
