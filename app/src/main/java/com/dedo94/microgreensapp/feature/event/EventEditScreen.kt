package com.dedo94.microgreensapp.feature.event

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import com.dedo94.microgreensapp.core.database.entity.ActionType
import com.dedo94.microgreensapp.ui.CompactHeader
import com.dedo94.microgreensapp.ui.DatePickerField
import com.dedo94.microgreensapp.ui.displayLabel
import com.dedo94.microgreensapp.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventEditScreen(
    onBack: () -> Unit,
    viewModel: EventEditViewModel = hiltViewModel(),
) {
    var typeMenuExpanded by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        CompactHeader(
            title = if (viewModel.isNew) "Nuovo evento" else "Modifica evento",
            onBack = onBack,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(Spacing.md)
                .verticalScroll(rememberScrollState()),
        ) {
            ExposedDropdownMenuBox(
                expanded = typeMenuExpanded,
                onExpandedChange = { typeMenuExpanded = it },
            ) {
                OutlinedTextField(
                    value = viewModel.eventType.displayLabel(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipo") },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeMenuExpanded)
                    },
                )
                ExposedDropdownMenu(
                    expanded = typeMenuExpanded,
                    onDismissRequest = { typeMenuExpanded = false },
                ) {
                    ActionType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.displayLabel()) },
                            onClick = {
                                viewModel.onTypeChange(type)
                                typeMenuExpanded = false
                            },
                        )
                    }
                }
            }

            Spacer(Modifier.height(Spacing.sm))
            OutlinedTextField(
                value = viewModel.title,
                onValueChange = viewModel::onTitleChange,
                label = { Text("Titolo") },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(Spacing.sm))
            Row(Modifier.fillMaxWidth()) {
                DatePickerField(
                    label = "Data",
                    date = viewModel.eventDate,
                    onDateChange = viewModel::onDateChange,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(Spacing.sm))
                OutlinedTextField(
                    value = viewModel.eventTimeText,
                    onValueChange = viewModel::onTimeTextChange,
                    label = { Text("Ora (opzionale, HH:mm)") },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(Spacing.sm))
            Row(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = viewModel.quantityText,
                    onValueChange = viewModel::onQuantityChange,
                    label = { Text("Quantità (opzionale)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(Spacing.sm))
                OutlinedTextField(
                    value = viewModel.quantityUnit,
                    onValueChange = viewModel::onQuantityUnitChange,
                    label = { Text("Unità (es. ml, g)") },
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(Spacing.sm))
            OutlinedTextField(
                value = viewModel.notes,
                onValueChange = viewModel::onNotesChange,
                label = { Text("Note") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(Spacing.lg))
            Text(
                text = "Condizioni ambientali",
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = "Precompilate dal meteo della posizione impostata, modificabili perché in casa le condizioni possono essere diverse.",
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(Modifier.height(Spacing.sm))
            Row(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = viewModel.temperatureText,
                    onValueChange = viewModel::onTemperatureChange,
                    label = { Text("Temperatura (°C)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(Spacing.sm))
                OutlinedTextField(
                    value = viewModel.humidityText,
                    onValueChange = viewModel::onHumidityChange,
                    label = { Text("Umidità (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(Spacing.sm))
            OutlinedTextField(
                value = viewModel.lightNotes,
                onValueChange = viewModel::onLightNotesChange,
                label = { Text("Note su luce/esposizione (opzionale)") },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Button(
            onClick = { viewModel.save(onBack) },
            enabled = viewModel.canSave,
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
        ) {
            Text("Salva")
        }
    }
}
