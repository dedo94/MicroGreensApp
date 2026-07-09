package com.dedo94.microgreensapp.feature.tray

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(trays, key = { it.id }) { tray ->
                    ListItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenTray(tray.id) },
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
            }
        }
    }
}
