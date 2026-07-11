package com.dedo94.microgreensapp.feature.tray

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dedo94.microgreensapp.core.database.entity.ActionType
import com.dedo94.microgreensapp.core.database.entity.EventEntity
import com.dedo94.microgreensapp.core.database.entity.TrayStatus
import com.dedo94.microgreensapp.core.database.entity.TrayStepEntity
import com.dedo94.microgreensapp.core.database.entity.TrayStepStatus
import com.dedo94.microgreensapp.ui.CompactHeader
import com.dedo94.microgreensapp.ui.displayLabel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrayDetailScreen(
    onBack: () -> Unit,
    onAddEvent: (Long) -> Unit,
    onEditEvent: (Long, Long) -> Unit,
    onEditTray: (Long) -> Unit,
    viewModel: TrayDetailViewModel = hiltViewModel(),
) {
    val tray by viewModel.tray.collectAsStateWithLifecycle()
    val steps by viewModel.steps.collectAsStateWithLifecycle()
    val events by viewModel.events.collectAsStateWithLifecycle()
    val harvestPrediction by viewModel.harvestPrediction.collectAsStateWithLifecycle()
    val timeline = remember(steps, events) { buildTimeline(steps, events) }

    var stepBeingEdited by remember { mutableStateOf<TrayStepEntity?>(null) }
    var stepPendingDeletion by remember { mutableStateOf<TrayStepEntity?>(null) }
    var stepPendingFutureConfirmation by remember { mutableStateOf<TrayStepEntity?>(null) }
    var stepPendingQuantityInput by remember { mutableStateOf<TrayStepEntity?>(null) }
    var eventPendingDeletion by remember { mutableStateOf<EventEntity?>(null) }
    var showStatusMenu by remember { mutableStateOf(false) }
    var showDeleteTrayDialog by remember { mutableStateOf(false) }

    // Per gli step di raccolta/irrigazione, prima di segnarli fatti chiediamo
    // la quantità (grammi raccolti o ml d'acqua): altrimenti non ci sarebbe
    // mai un punto in cui inserirla.
    fun proceedMarkDone(step: TrayStepEntity) {
        if (step.actionType == ActionType.HARVEST || step.actionType == ActionType.WATERING) {
            stepPendingQuantityInput = step
        } else {
            viewModel.markDone(step)
        }
    }

    Column(Modifier.fillMaxSize()) {
        CompactHeader(
            title = tray?.name ?: "",
            onBack = onBack,
            actions = {
                Box {
                    IconButton(onClick = { showStatusMenu = true }) {
                        Icon(Icons.Outlined.MoreVert, contentDescription = "Altre azioni")
                    }
                    DropdownMenu(
                        expanded = showStatusMenu,
                        onDismissRequest = { showStatusMenu = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Modifica vassoio") },
                            onClick = {
                                showStatusMenu = false
                                onEditTray(viewModel.trayId)
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Segna come raccolto") },
                            onClick = {
                                viewModel.setStatus(TrayStatus.HARVESTED)
                                showStatusMenu = false
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Segna come in corso") },
                            onClick = {
                                viewModel.setStatus(TrayStatus.IN_PROGRESS)
                                showStatusMenu = false
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Elimina vassoio") },
                            onClick = {
                                showStatusMenu = false
                                showDeleteTrayDialog = true
                            },
                        )
                    }
                }
            },
        )
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            tray?.let { t ->
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = "${t.varietyName} · ${t.status.displayLabel()}",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    val seedInfo = t.initialSeedQuantityGrams?.let { " · Semi: ${it}g" } ?: ""
                    Text(
                        text = "Semina: ${t.sowingDate}$seedInfo",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    val substrateInfo = if (t.substrateNotes.isNotBlank()) " (${t.substrateNotes})" else ""
                    Text(
                        text = "Substrato: ${t.substrateType.displayLabel()}$substrateInfo",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    if (t.status == TrayStatus.IN_PROGRESS) {
                        harvestPrediction?.let { prediction ->
                            Text(
                                text = "Previsione raccolto: ~${"%.1f".format(prediction.predictedGrams)}g " +
                                    "(basata su ${prediction.basedOnCycles} " +
                                    if (prediction.basedOnCycles == 1) "ciclo precedente)" else "cicli precedenti)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
                HorizontalDivider()
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
            ) {
                items(
                    items = timeline,
                    key = { entry ->
                        when (entry) {
                            is TrayTimelineEntry.StepEntry -> "step-${entry.step.id}"
                            is TrayTimelineEntry.EventEntry -> "event-${entry.event.id}"
                        }
                    },
                ) { entry ->
                    when (entry) {
                        is TrayTimelineEntry.StepEntry -> StepTimelineCard(
                            step = entry.step,
                            onMarkDone = {
                                if (entry.step.plannedStartDate.isAfter(LocalDate.now())) {
                                    stepPendingFutureConfirmation = entry.step
                                } else {
                                    proceedMarkDone(entry.step)
                                }
                            },
                            onMarkSkipped = { viewModel.markSkipped(entry.step) },
                            onMarkPending = { viewModel.markPending(entry.step) },
                            onEdit = { stepBeingEdited = entry.step },
                            onDelete = if (entry.step.isAdHoc) {
                                { stepPendingDeletion = entry.step }
                            } else null,
                        )

                        is TrayTimelineEntry.EventEntry -> EventTimelineCard(
                            event = entry.event,
                            onEdit = { onEditEvent(viewModel.trayId, entry.event.id) },
                            onDelete = { eventPendingDeletion = entry.event },
                        )
                    }
                }
                item(key = "add-event") {
                    AddEventCard(onClick = { onAddEvent(viewModel.trayId) })
                }
            }
        }
    }

    stepBeingEdited?.let { step ->
        TrayStepEditDialog(
            step = step,
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

    stepPendingFutureConfirmation?.let { step ->
        AlertDialog(
            onDismissRequest = { stepPendingFutureConfirmation = null },
            title = { Text("Confermare in anticipo?") },
            text = {
                Text(
                    "\"${step.name}\" è previsto per il ${step.plannedStartDate}, una data futura. " +
                        "Vuoi segnarlo comunque come fatto?"
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    stepPendingFutureConfirmation = null
                    proceedMarkDone(step)
                }) { Text("Conferma") }
            },
            dismissButton = {
                TextButton(onClick = { stepPendingFutureConfirmation = null }) { Text("Annulla") }
            },
        )
    }

    stepPendingQuantityInput?.let { step ->
        val isHarvest = step.actionType == ActionType.HARVEST
        var quantityText by remember(step.id) { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { stepPendingQuantityInput = null },
            title = { Text(if (isHarvest) "Registra il raccolto" else "Registra l'irrigazione") },
            text = {
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text(if (isHarvest) "Quantità raccolta (g)" else "Acqua data (ml)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.markDone(
                        step = step,
                        quantityValue = quantityText.toDoubleOrNull(),
                        quantityUnit = if (isHarvest) "g" else "ml",
                    )
                    stepPendingQuantityInput = null
                }) { Text("Conferma") }
            },
            dismissButton = {
                TextButton(onClick = { stepPendingQuantityInput = null }) { Text("Annulla") }
            },
        )
    }

    eventPendingDeletion?.let { event ->
        AlertDialog(
            onDismissRequest = { eventPendingDeletion = null },
            title = { Text("Eliminare l'evento \"${event.title}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteEvent(event)
                    eventPendingDeletion = null
                }) { Text("Elimina") }
            },
            dismissButton = {
                TextButton(onClick = { eventPendingDeletion = null }) { Text("Annulla") }
            },
        )
    }

    if (showDeleteTrayDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteTrayDialog = false },
            title = { Text("Eliminare questo vassoio?") },
            text = { Text("Tutti gli step e gli eventi registrati verranno eliminati. L'operazione non è reversibile.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteTrayDialog = false
                    viewModel.deleteTray(onBack)
                }) { Text("Elimina") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteTrayDialog = false }) { Text("Annulla") }
            },
        )
    }
}

/**
 * Chiusura naturale della timeline: una card identica a quelle degli step
 * con un + centrato, al posto di un FAB che galleggiava sopra il contenuto.
 */
@Composable
private fun AddEventCard(onClick: () -> Unit) {
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
            Icon(Icons.Outlined.Add, contentDescription = "Aggiungi evento")
        }
    }
}

@Composable
private fun StepTimelineCard(
    step: TrayStepEntity,
    onMarkDone: () -> Unit,
    onMarkSkipped: () -> Unit,
    onMarkPending: () -> Unit,
    onEdit: () -> Unit,
    onDelete: (() -> Unit)?,
) {
    Card(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(Modifier.weight(1f)) {
                Text(step.name, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = "${step.actionType.displayLabel()} · ${dateRangeText(step)}${step.status.displayLabel()}",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Row {
                if (step.status == TrayStepStatus.PENDING) {
                    IconButton(onClick = onMarkDone) {
                        Icon(Icons.Outlined.Check, contentDescription = "Segna come fatto")
                    }
                    IconButton(onClick = onMarkSkipped) {
                        Icon(Icons.Outlined.Close, contentDescription = "Salta")
                    }
                } else {
                    IconButton(onClick = onMarkPending) {
                        Icon(Icons.Outlined.Replay, contentDescription = "Annulla completamento")
                    }
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Outlined.Edit, contentDescription = "Modifica")
                }
                if (onDelete != null) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Outlined.Delete, contentDescription = "Elimina")
                    }
                }
            }
        }
    }
}

private fun dateRangeText(step: TrayStepEntity): String =
    if (step.plannedEndDate != step.plannedStartDate) {
        "${step.plannedStartDate} → ${step.plannedEndDate} · "
    } else {
        "${step.plannedStartDate} · "
    }

@Composable
private fun EventTimelineCard(
    event: EventEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(Modifier.weight(1f)) {
                Text(event.title, style = MaterialTheme.typography.titleSmall)
                val quantityInfo = event.quantityValue?.let { " · ${it}${event.quantityUnit}" } ?: ""
                Text(
                    text = "${event.eventType.displayLabel()} · ${event.eventDate}$quantityInfo",
                    style = MaterialTheme.typography.bodySmall,
                )
                if (event.notes.isNotBlank()) {
                    Text(event.notes, style = MaterialTheme.typography.bodySmall)
                }
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Outlined.Edit, contentDescription = "Modifica")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Elimina")
                }
            }
        }
    }
}
