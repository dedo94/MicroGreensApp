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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dedo94.microgreensapp.core.database.entity.TrayEntity
import com.dedo94.microgreensapp.core.database.entity.TrayStatus
import com.dedo94.microgreensapp.ui.displayColor
import com.dedo94.microgreensapp.ui.displayLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TraysListScreen(
    onCreateTray: () -> Unit,
    onOpenTray: (Long) -> Unit,
    onManageVarieties: () -> Unit,
    viewModel: TraysListViewModel = hiltViewModel(),
) {
    val trays by viewModel.trays.collectAsStateWithLifecycle()
    val sections = remember(trays) {
        listOf(
            "In corso" to trays.filter { it.status == TrayStatus.IN_PROGRESS },
            "Raccolti" to trays.filter { it.status == TrayStatus.HARVESTED },
            "Abbandonati" to trays.filter { it.status == TrayStatus.ABANDONED },
        ).filter { it.second.isNotEmpty() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Vassoi") },
                actions = {
                    IconButton(onClick = onManageVarieties) {
                        Icon(Icons.Default.Eco, contentDescription = "Gestisci varietà")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateTray) {
                Icon(Icons.Default.Add, contentDescription = "Nuovo vassoio")
            }
        },
    ) { padding ->
        if (trays.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "Nessun vassoio ancora. Tocca + per iniziare una coltivazione.",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(bottom = 96.dp),
            ) {
                sections.forEach { (title, sectionTrays) ->
                    item(key = "header-$title") {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }
                    items(sectionTrays, key = { it.id }) { tray ->
                        TrayListItem(tray = tray, onClick = { onOpenTray(tray.id) })
                    }
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
