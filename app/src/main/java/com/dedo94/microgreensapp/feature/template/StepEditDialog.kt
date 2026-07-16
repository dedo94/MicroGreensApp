package com.dedo94.microgreensapp.feature.template

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
import com.dedo94.microgreensapp.core.database.entity.ActionType
import com.dedo94.microgreensapp.core.database.entity.TemplateStepEntity
import com.dedo94.microgreensapp.ui.displayLabel
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StepEditDialog(
    initialStep: TemplateStepEntity?,
    onDismiss: () -> Unit,
    onConfirm: (TemplateStepEntity) -> Unit,
) {
    var name by remember { mutableStateOf(initialStep?.name ?: "") }
    var actionType by remember { mutableStateOf(initialStep?.actionType ?: ActionType.CUSTOM) }
    var offsetStartDaysText by remember { mutableStateOf((initialStep?.offsetStartDays ?: 0).toString()) }
    var hasRange by remember { mutableStateOf(initialStep?.offsetEndDays != null) }
    var offsetEndDaysText by remember {
        mutableStateOf((initialStep?.offsetEndDays ?: initialStep?.offsetStartDays ?: 0).toString())
    }
    var hasDuration by remember { mutableStateOf(initialStep?.durationHours != null) }
    var durationHoursText by remember { mutableStateOf((initialStep?.durationHours ?: 1).toString()) }
    var reminderTimesText by remember {
        mutableStateOf(initialStep?.reminderTimes?.joinToString(", ") { it.toString() } ?: "")
    }
    var instructions by remember { mutableStateOf(initialStep?.instructions ?: "") }
    var actionTypeMenuExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialStep == null) "Nuovo step" else "Modifica step") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome step") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = actionTypeMenuExpanded,
                    onExpandedChange = { actionTypeMenuExpanded = it },
                ) {
                    OutlinedTextField(
                        value = actionType.displayLabel(),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo azione") },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth(),
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = actionTypeMenuExpanded)
                        },
                    )
                    ExposedDropdownMenu(
                        expanded = actionTypeMenuExpanded,
                        onDismissRequest = { actionTypeMenuExpanded = false },
                    ) {
                        ActionType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.displayLabel()) },
                                onClick = {
                                    actionType = type
                                    actionTypeMenuExpanded = false
                                },
                            )
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Giorno relativo all'inizio della fase (0 = primo giorno della fase)",
                    style = MaterialTheme.typography.bodySmall,
                )
                Row(Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = offsetStartDaysText,
                        onValueChange = { offsetStartDaysText = it.filter(Char::isDigit) },
                        label = { Text("Giorno inizio") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                    if (hasRange) {
                        Spacer(Modifier.width(8.dp))
                        OutlinedTextField(
                            value = offsetEndDaysText,
                            onValueChange = { offsetEndDaysText = it.filter(Char::isDigit) },
                            label = { Text("Giorno fine") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = hasRange, onCheckedChange = { hasRange = it })
                    Text("Intervallo di più giorni")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = hasDuration, onCheckedChange = { hasDuration = it })
                    Text("Durata in ore (es. ammollo)")
                }
                if (hasDuration) {
                    OutlinedTextField(
                        value = durationHoursText,
                        onValueChange = { durationHoursText = it.filter(Char::isDigit) },
                        label = { Text("Durata (ore)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                }
                OutlinedTextField(
                    value = reminderTimesText,
                    onValueChange = { reminderTimesText = it },
                    label = { Text("Orari promemoria (es. 08:00, 20:00)") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = "Un orario = un promemoria e un'occorrenza segnabile a parte (es. due orari = mattina e sera separate). Vuoto = una volta al giorno senza promemoria.",
                    style = MaterialTheme.typography.bodySmall,
                )
                OutlinedTextField(
                    value = instructions,
                    onValueChange = { instructions = it },
                    label = { Text("Istruzioni") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = {
                    val times = reminderTimesText.split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                        .mapNotNull { runCatching { LocalTime.parse(it) }.getOrNull() }
                    val base = initialStep ?: TemplateStepEntity(
                        phaseId = 0L,
                        orderIndex = 0,
                        name = "",
                        actionType = ActionType.CUSTOM,
                        offsetStartDays = 0,
                    )
                    val step = base.copy(
                        name = name,
                        actionType = actionType,
                        offsetStartDays = offsetStartDaysText.toIntOrNull() ?: 0,
                        offsetEndDays = if (hasRange) offsetEndDaysText.toIntOrNull() else null,
                        durationHours = if (hasDuration) durationHoursText.toIntOrNull() else null,
                        reminderTimes = times,
                        instructions = instructions,
                    )
                    onConfirm(step)
                },
            ) { Text("Salva") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annulla") } },
    )
}
