package com.dedo94.microgreensapp.feature.tray

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dedo94.microgreensapp.core.database.entity.TrayEntity
import com.dedo94.microgreensapp.core.database.entity.TrayStatus
import com.dedo94.microgreensapp.ui.CompactHeader
import com.dedo94.microgreensapp.ui.displayColor
import com.dedo94.microgreensapp.ui.displayLabel

@Composable
fun TraysListScreen(
    onOpenTray: (Long) -> Unit,
    viewModel: TraysListViewModel = hiltViewModel(),
) {
    val trays by viewModel.trays.collectAsStateWithLifecycle()
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val visibleTrays = remember(trays, selectedTab) {
        val status = if (selectedTab == 0) TrayStatus.IN_PROGRESS else TrayStatus.HARVESTED
        trays.filter { it.status == status }
    }

    Column(Modifier.fillMaxSize()) {
        CompactHeader("Vassoi")

        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("In corso") },
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Raccolti") },
            )
        }

        if (visibleTrays.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = if (selectedTab == 0) {
                        "Nessun vassoio in corso. Tocca + nella barra in basso per iniziare una coltivazione."
                    } else {
                        "Nessun vassoio raccolto finora."
                    },
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp),
            ) {
                items(visibleTrays, key = { it.id }) { tray ->
                    TrayListItem(tray = tray, onClick = { onOpenTray(tray.id) })
                }
            }
        }
    }
}

@Composable
private fun TrayListItem(tray: TrayEntity, onClick: () -> Unit) {
    ListItem(
        modifier = Modifier
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
            Text("${tray.varietyName} · ${tray.status.displayLabel()} · semina ${tray.sowingDate}")
        },
    )
}
