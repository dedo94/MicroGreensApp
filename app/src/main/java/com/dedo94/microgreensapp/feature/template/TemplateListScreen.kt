package com.dedo94.microgreensapp.feature.template

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dedo94.microgreensapp.core.database.entity.VarietyTemplateEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateListScreen(
    onCreateTemplate: () -> Unit,
    onOpenTemplate: (Long) -> Unit,
    viewModel: TemplateListViewModel = hiltViewModel(),
) {
    val templates by viewModel.templates.collectAsStateWithLifecycle()
    var templateToDelete by remember { mutableStateOf<VarietyTemplateEntity?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Varietà") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateTemplate) {
                Icon(Icons.Default.Add, contentDescription = "Nuova varietà")
            }
        },
    ) { padding ->
        if (templates.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = "Nessuna varietà ancora. Tocca + per crearne una.",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(templates, key = { it.id }) { template ->
                    ListItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenTemplate(template.id) },
                        leadingContent = { Icon(Icons.Default.Eco, contentDescription = null) },
                        headlineContent = { Text(template.name) },
                        supportingContent = { Text(template.plantType) },
                        trailingContent = {
                            IconButton(onClick = { templateToDelete = template }) {
                                Icon(Icons.Default.Delete, contentDescription = "Elimina")
                            }
                        },
                    )
                }
            }
        }
    }

    templateToDelete?.let { template ->
        AlertDialog(
            onDismissRequest = { templateToDelete = null },
            title = { Text("Eliminare \"${template.name}\"?") },
            text = { Text("Verranno eliminati anche tutti gli step del template. L'operazione non è reversibile.") },
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
