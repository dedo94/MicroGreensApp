package com.dedo94.microgreensapp.feature.template

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.dedo94.microgreensapp.core.database.entity.VarietyTemplateEntity
import com.dedo94.microgreensapp.ui.CompactHeader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateListScreen(
    onBack: () -> Unit,
    onCreateTemplate: () -> Unit,
    onOpenTemplate: (Long) -> Unit,
    viewModel: TemplateListViewModel = hiltViewModel(),
) {
    val templates by viewModel.templates.collectAsStateWithLifecycle()
    var templateToDelete by remember { mutableStateOf<VarietyTemplateEntity?>(null) }

    Column(Modifier.fillMaxSize()) {
        CompactHeader(title = "Varietà", onBack = onBack)
        if (templates.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "Nessuna varietà ancora. Tocca + qui sotto per crearne una.",
                    style = MaterialTheme.typography.bodyLarge,
                )
                AddTemplateCard(onClick = onCreateTemplate)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
            ) {
                items(templates, key = { it.id }) { template ->
                    ListItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenTemplate(template.id) },
                        leadingContent = { Icon(Icons.Outlined.Eco, contentDescription = null) },
                        headlineContent = { Text(template.name) },
                        supportingContent = { Text(template.plantType) },
                        trailingContent = {
                            IconButton(onClick = { templateToDelete = template }) {
                                Icon(Icons.Outlined.Delete, contentDescription = "Elimina")
                            }
                        },
                    )
                }
                item(key = "add-template") {
                    AddTemplateCard(onClick = onCreateTemplate)
                }
            }
        }
    }

    templateToDelete?.let { template ->
        AlertDialog(
            onDismissRequest = { templateToDelete = null },
            title = { Text("Eliminare \"${template.name}\"?") },
            text = { Text("Se non è mai stato usato per un vassoio verrà eliminato definitivamente insieme ai suoi step; altrimenti verrà solo archiviato e non comparirà più tra le varietà disponibili per nuovi vassoi.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteTemplate(template)
                    templateToDelete = null
                }) { Text("Elimina") }
            },
            dismissButton = {
                TextButton(onClick = { templateToDelete = null }) { Text("Annulla") }
            },
        )
    }
}

@Composable
private fun AddTemplateCard(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.Add, contentDescription = "Nuova varietà")
        }
    }
}
