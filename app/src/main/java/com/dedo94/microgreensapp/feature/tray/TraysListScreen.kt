package com.dedo94.microgreensapp.feature.tray

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dedo94.microgreensapp.core.database.entity.TrayStatus
import com.dedo94.microgreensapp.ui.CompactHeader
import com.dedo94.microgreensapp.ui.displayColor
import com.dedo94.microgreensapp.ui.theme.Spacing
import kotlinx.coroutines.launch

@Composable
fun TraysListScreen(
    onOpenTray: (Long) -> Unit,
    viewModel: TraysListViewModel = hiltViewModel(),
) {
    val trays by viewModel.trays.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(pageCount = { 2 })
    val coroutineScope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize()) {
        CompactHeader("Vassoi")

        TabRow(selectedTabIndex = pagerState.currentPage) {
            Tab(
                selected = pagerState.currentPage == 0,
                onClick = { coroutineScope.launch { pagerState.animateScrollToPage(0) } },
                text = { Text("In corso") },
            )
            Tab(
                selected = pagerState.currentPage == 1,
                onClick = { coroutineScope.launch { pagerState.animateScrollToPage(1) } },
                text = { Text("Raccolti") },
            )
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            val status = if (page == 0) TrayStatus.IN_PROGRESS else TrayStatus.HARVESTED
            val visibleTrays = remember(trays, status) { trays.filter { it.tray.status == status } }
            TrayListPage(page = page, trays = visibleTrays, onOpenTray = onOpenTray)
        }
    }
}

@Composable
private fun TrayListPage(
    page: Int,
    trays: List<TrayListItemUiState>,
    onOpenTray: (Long) -> Unit,
) {
    if (trays.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.xl),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = Icons.Outlined.Eco,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(Spacing.md))
            Text(
                text = if (page == 0) {
                    "Nessun vassoio in corso. Tocca + nella barra in basso per iniziare una coltivazione."
                } else {
                    "Nessun vassoio raccolto finora."
                },
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = Spacing.md),
            contentPadding = PaddingValues(bottom = Spacing.md),
        ) {
            items(trays, key = { it.tray.id }) { item ->
                if (page == 0) {
                    TrayDashboardCard(
                        item = item,
                        modifier = Modifier.animateItem(),
                        onClick = { onOpenTray(item.tray.id) },
                    )
                } else {
                    TrayListItem(
                        item = item,
                        modifier = Modifier.animateItem(),
                        onClick = { onOpenTray(item.tray.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun TrayDashboardCard(
    item: TrayListItemUiState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xs)
            .animateContentSize(),
    ) {
        Column(Modifier.padding(Spacing.md)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(item.tray.displayColor()),
                )
                Spacer(Modifier.width(Spacing.sm))
                Column(Modifier.weight(1f)) {
                    Text(item.tray.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "${item.tray.varietyName} · giorno ${item.daysSinceSowing}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            val progress = item.progress
            if (progress != null) {
                Spacer(Modifier.height(Spacing.sm))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Spacing.xs)),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    text = harvestCountdownText(item.plannedCycleDays?.minus(item.daysSinceSowing)),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

private fun harvestCountdownText(remainingDays: Long?): String = when {
    remainingDays == null -> ""
    remainingDays > 0 -> "$remainingDays giorni al raccolto stimato"
    remainingDays == 0L -> "Raccolto stimato oggi"
    else -> "Raccolto stimato superato di ${-remainingDays} giorni"
}

@Composable
private fun TrayListItem(
    item: TrayListItemUiState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val tray = item.tray
    val harvestInfo = item.harvestTotalGrams?.let { "${"%.1f".format(it)}g" } ?: "—"
    val durationInfo = item.actualCycleDays?.let { "$it giorni" } ?: "—"
    ListItem(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(tray.displayColor()),
            )
        },
        headlineContent = { Text(tray.name) },
        supportingContent = {
            Text("${tray.varietyName} · raccolto $harvestInfo · durata $durationInfo")
        },
    )
}
