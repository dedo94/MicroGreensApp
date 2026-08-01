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
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DragHandle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dedo94.microgreensapp.core.database.entity.TemplatePhaseEntity
import com.dedo94.microgreensapp.ui.BottomSheetForm
import com.dedo94.microgreensapp.ui.CompactHeader
import com.dedo94.microgreensapp.ui.DashedAddCard
import com.dedo94.microgreensapp.ui.theme.Spacing
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateEditScreen(
    onBack: () -> Unit,
    onOpenPhase: (Long, Long) -> Unit,
    viewModel: TemplateEditViewModel = hiltViewModel(),
) {
    val phases by viewModel.phases.collectAsStateWithLifecycle()
    val templateId by viewModel.templateId.collectAsStateWithLifecycle()

    var showNewPhaseDialog by remember { mutableStateOf(false) }
    var showDeleteTemplateDialog by remember { mutableStateOf(false) }
    var phasePendingDeletion by remember { mutableStateOf<TemplatePhaseEntity?>(null) }

    // Stato locale ottimistico: aggiornato subito ad ogni spostamento durante
    // il trascinamento (nessun I/O), persistito una sola volta a fine gesto
    // in onDragStopped — vedi la stessa soluzione già applicata al riordino
    // degli step, per evitare la race condition di scritture Room sovrapposte.
    val localPhases = remember { mutableStateListOf<TemplatePhaseEntity>() }
    LaunchedEffect(phases) {
        localPhases.clear()
        localPhases.addAll(phases)
    }
    val lazyListState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        localPhases.add(to.index, localPhases.removeAt(from.index))
    }

    Column(Modifier.fillMaxSize()) {
        CompactHeader(
            title = if (viewModel.isNew) "Nuova varietà" else "Modifica varietà",
            onBack = onBack,
            actions = {
                if (templateId != null) {
                    IconButton(onClick = { showDeleteTemplateDialog = true }) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Elimina varietà")
                    }
                }
            },
        )
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = Spacing.md),
            contentPadding = PaddingValues(bottom = Spacing.md),
        ) {
            item {
                Column(Modifier.padding(vertical = Spacing.sm)) {
                    OutlinedTextField(
                        value = viewModel.name,
                        onValueChange = viewModel::onNameChange,
                        label = { Text("Nome varietà") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(Spacing.sm))
                    OutlinedTextField(
                        value = viewModel.plantType,
                        onValueChange = viewModel::onPlantTypeChange,
                        label = { Text("Tipo pianta") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(Spacing.sm))
                    OutlinedTextField(
                        value = viewModel.notes,
                        onValueChange = viewModel::onNotesChange,
                        label = { Text("Note") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                    )
                    if (viewModel.isNew && templateId == null) {
                        Spacer(Modifier.height(Spacing.sm))
                        Text(
                            text = "Salva le informazioni base per poter aggiungere le fasi.",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    } else if (!viewModel.isInfoSaved) {
                        Spacer(Modifier.height(Spacing.sm))
                        Text(
                            text = "Modifiche non salvate. Tocca ✓ per salvare.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(top = Spacing.md))
                    Text(
                        text = "Fasi del ciclo di coltivazione",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = Spacing.sm),
                    )
                }
            }

            items(localPhases, key = { it.id }) { phase ->
                ReorderableItem(reorderableState, key = phase.id) { _ ->
                    Card(modifier = Modifier.padding(vertical = Spacing.xs)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Spacing.sm),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                IconButton(
                                    modifier = Modifier.draggableHandle(
                                        onDragStopped = { viewModel.reorderPhases(localPhases.toList()) },
                                    ),
                                    onClick = {},
                                ) {
                                    Icon(Icons.Outlined.DragHandle, contentDescription = "Riordina")
                                }
                                Spacer(Modifier.width(Spacing.sm))
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            templateId?.let { onOpenPhase(it, phase.id) }
                                        },
                                ) {
                                    Text(phase.name, style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        text = phaseSubtitle(phase),
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                            }
                            IconButton(onClick = { phasePendingDeletion = phase }) {
                                Icon(Icons.Outlined.Delete, contentDescription = "Elimina fase")
                            }
                        }
                    }
                }
            }

            if (templateId != null) {
                item(key = "add-phase") {
                    DashedAddCard(
                        onClick = { showNewPhaseDialog = true },
                        contentDescription = "Aggiungi fase",
                        modifier = Modifier.padding(vertical = Spacing.xs),
                    )
                }
            }
        }
        Button(
            onClick = { viewModel.saveTemplateInfo() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
        ) {
            Text("Salva")
        }
    }

    if (showNewPhaseDialog) {
        PhaseNameDialog(
            initialName = "",
            onDismiss = { showNewPhaseDialog = false },
            onConfirm = { name ->
                viewModel.addPhase(name)
                showNewPhaseDialog = false
            },
        )
    }

    phasePendingDeletion?.let { phase ->
        AlertDialog(
            onDismissRequest = { phasePendingDeletion = null },
            title = { Text("Eliminare la fase \"${phase.name}\"?") },
            text = { Text("Tutti gli step contenuti in questa fase verranno eliminati.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deletePhase(phase)
                    phasePendingDeletion = null
                }) { Text("Elimina") }
            },
            dismissButton = {
                TextButton(onClick = { phasePendingDeletion = null }) { Text("Annulla") }
            },
        )
    }

    if (showDeleteTemplateDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteTemplateDialog = false },
            title = { Text("Eliminare questa varietà?") },
            text = { Text("Se non è mai stato usato per un vassoio verrà eliminato definitivamente insieme alle sue fasi; altrimenti verrà solo archiviato e non comparirà più tra le varietà disponibili per nuovi vassoi.") },
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

private fun phaseSubtitle(phase: TemplatePhaseEntity): String =
    phase.durationDays?.let { "${it} giorn${if (it == 1) "o" else "i"}" } ?: "Durata aperta"

/** Solo il nome: durata e step si impostano entrando nel dettaglio della fase appena creata. */
@Composable
private fun PhaseNameDialog(
    initialName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    BottomSheetForm(
        title = "Nuova fase",
        onDismiss = onDismiss,
        onConfirm = { onConfirm(name) },
        confirmLabel = "Crea",
        confirmEnabled = name.isNotBlank(),
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nome fase (es. Germinazione)") },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
