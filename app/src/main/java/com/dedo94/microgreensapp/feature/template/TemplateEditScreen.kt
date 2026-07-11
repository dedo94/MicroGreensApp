package com.dedo94.microgreensapp.feature.template

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.dedo94.microgreensapp.core.database.entity.TemplateStepEntity
import com.dedo94.microgreensapp.ui.displayLabel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateEditScreen(
    onBack: () -> Unit,
    viewModel: TemplateEditViewModel = hiltViewModel(),
) {
    val steps by viewModel.steps.collectAsStateWithLifecycle()
    val templateId by viewModel.templateId.collectAsStateWithLifecycle()

    var stepBeingEdited by remember { mutableStateOf<TemplateStepEntity?>(null) }
    var showNewStepDialog by remember { mutableStateOf(false) }
    var showDeleteTemplateDialog by remember { mutableStateOf(false) }
    var stepPendingDeletion by remember { mutableStateOf<TemplateStepEntity?>(null) }

    val lazyListState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        viewModel.moveStep(from.index, to.index)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (viewModel.isNew) "Nuova varietà" else "Modifica varietà") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.saveTemplateInfo() }) {
                        Icon(Icons.Default.Check, contentDescription = "Salva")
                    }
                    if (templateId != null) {
                        IconButton(onClick = { showDeleteTemplateDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Elimina varietà")
                        }
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            item {
                Column(Modifier.padding(vertical = 12.dp)) {
                    OutlinedTextField(
                        value = viewModel.name,
                        onValueChange = viewModel::onNameChange,
                        label = { Text("Nome varietà") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = viewModel.plantType,
                        onValueChange = viewModel::onPlantTypeChange,
                        label = { Text("Tipo pianta") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = viewModel.notes,
                        onValueChange = viewModel::onNotesChange,
                        label = { Text("Note") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                    )
                    if (viewModel.isNew && templateId == null) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Salva le informazioni base per poter aggiungere gli step.",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    } else if (!viewModel.isInfoSaved) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Modifiche non salvate. Tocca ✓ per salvare.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(top = 16.dp))
                    Text(
                        text = "Step del piano di coltivazione",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 12.dp),
                    )
                }
            }

            items(steps, key = { it.id }) { step ->
                ReorderableItem(reorderableState, key = step.id) { _ ->
                    Card(modifier = Modifier.padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                IconButton(
                                    modifier = Modifier.draggableHandle(),
                                    onClick = {},
                                ) {
                                    Icon(Icons.Default.DragHandle, contentDescription = "Riordina")
                                }
                                Spacer(Modifier.width(8.dp))
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { stepBeingEdited = step },
                                ) {
                                    Text(step.name, style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        text = stepSubtitle(step),
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                            }
                            IconButton(onClick = { stepPendingDeletion = step }) {
                                Icon(Icons.Default.Delete, contentDescription = "Elimina step")
                            }
                        }
                    }
                }
            }

            if (templateId != null) {
                item(key = "add-step") {
                    AddStepCard(onClick = { showNewStepDialog = true })
                }
            }
        }
    }

    if (showNewStepDialog) {
        StepEditDialog(
            initialStep = null,
            onDismiss = { showNewStepDialog = false },
            onConfirm = { step ->
                viewModel.addStep(step)
                showNewStepDialog = false
            },
        )
    }

    stepBeingEdited?.let { step ->
        StepEditDialog(
            initialStep = step,
            onDismiss = { stepBeingEdited = null },
            onConfirm = { updated ->
                viewModel.updateStep(updated)
                stepBeingEdited = null
            },
        )
    }

    stepPendingDeletion?.let { step ->
        AlertDialog(
            onDismissRequest = { stepPendingDeletion = null },
            title = { Text("Eliminare lo step \"${step.name}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteStep(step)
                    stepPendingDeletion = null
                }) { Text("Elimina") }
            },
            dismissButton = {
                TextButton(onClick = { stepPendingDeletion = null }) { Text("Annulla") }
            },
        )
    }

    if (showDeleteTemplateDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteTemplateDialog = false },
            title = { Text("Eliminare questa varietà?") },
            text = { Text("Se non è mai stato usato per un vassoio verrà eliminato definitivamente insieme ai suoi step; altrimenti verrà solo archiviato e non comparirà più tra le varietà disponibili per nuovi vassoi.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteTemplateDialog = false
                    viewModel.deleteTemplate(onDeleted = onBack)
                }) { Text("Elimina") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteTemplateDialog = false }) { Text("Annulla") }
            },
        )
    }
}

@Composable
private fun AddStepCard(onClick: () -> Unit) {
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
            Icon(Icons.Default.Add, contentDescription = "Aggiungi step")
        }
    }
}

private fun stepSubtitle(step: TemplateStepEntity): String {
    val dayRange = if (step.offsetEndDays != null && step.offsetEndDays != step.offsetStartDays) {
        "Giorni ${step.offsetStartDays}-${step.offsetEndDays}"
    } else {
        "Giorno ${step.offsetStartDays}"
    }
    val duration = step.durationHours?.let { " · ${it}h" } ?: ""
    return "${step.actionType.displayLabel()} · $dayRange$duration"
}
