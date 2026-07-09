package com.dedo94.microgreensapp.feature.tray

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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dedo94.microgreensapp.core.database.entity.ActionType
import com.dedo94.microgreensapp.core.database.entity.TrayStepEntity
import com.dedo94.microgreensapp.ui.DatePickerField
import com.dedo94.microgreensapp.ui.displayLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrayStepEditDialog(
    step: TrayStepEntity,
    onDismiss: () -> Unit,
    onConfirm: (TrayStepEntity) -> Unit,
) {
    var name by remember { mutableStateOf(step.name) }
    var actionType by remember { mutableStateOf(step.actionType) }
    var plannedStartDate by remember { mutableStateOf(step.plannedStartDate) }
    var plannedEndDate by remember { mutableStateOf(step.plannedEndDate) }
    var durationHoursText by remember { mutableStateOf(step.durationHours?.toString() ?: "") }
    var instructions by remember { mutableStateOf(step.instructions) }
    var actionTypeMenuExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Modifica step") },
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
                Row(Modifier.fillMaxWidth()) {
                    DatePickerField(
                        label = "Dal",
                        date = plannedStartDate,
                        onDateChange = {
                            plannedStartDate = it
                            if (plannedEndDate.isBefore(it)) plannedEndDate = it
                        },
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(8.dp))
                    DatePickerField(
                        label = "Al",
                        date = plannedEndDate,
                        onDateChange = { if (!it.isBefore(plannedStartDate)) plannedEndDate = it },
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = durationHoursText,
                    onValueChange = { durationHoursText = it.filter(Char::isDigit) },
                    label = { Text("Durata (ore, opzionale)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = instructions,
                    onValueChange = { instructions = it },
                    label = { Text("Istruzioni") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank(),
                onClick = {
                    onConfirm(
                        step.copy(
                            name = name,
                            actionType = actionType,
                            plannedStartDate = plannedStartDate,
                            plannedEndDate = plannedEndDate,
                            durationHours = durationHoursText.toIntOrNull(),
                            instructions = instructions,
                        )
                    )
                },
            ) { Text("Salva") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annulla") } },
    )
}
