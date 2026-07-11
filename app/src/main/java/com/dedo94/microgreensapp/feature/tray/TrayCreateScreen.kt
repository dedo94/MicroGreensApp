package com.dedo94.microgreensapp.feature.tray

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dedo94.microgreensapp.core.database.entity.SubstrateType
import com.dedo94.microgreensapp.ui.CompactHeader
import com.dedo94.microgreensapp.ui.DatePickerField
import com.dedo94.microgreensapp.ui.displayLabel
import com.dedo94.microgreensapp.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrayCreateScreen(
    onBack: () -> Unit,
    onManageVarieties: () -> Unit,
    onCreated: (Long) -> Unit,
    viewModel: TrayCreateViewModel = hiltViewModel(),
) {
    val templates by viewModel.templates.collectAsStateWithLifecycle()
    var templateMenuExpanded by remember { mutableStateOf(false) }
    var substrateMenuExpanded by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        CompactHeader(
            title = "Nuovo vassoio",
            onBack = onBack,
            actions = {
                IconButton(
                    onClick = { viewModel.save(onCreated) },
                    enabled = viewModel.canSave,
                ) {
                    Icon(Icons.Outlined.Check, contentDescription = "Crea vassoio")
                }
            },
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.md)
                .verticalScroll(rememberScrollState()),
        ) {
            ExposedDropdownMenuBox(
                expanded = templateMenuExpanded,
                onExpandedChange = { templateMenuExpanded = it },
            ) {
                OutlinedTextField(
                    value = viewModel.selectedTemplate?.name ?: "Scegli una varietà",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Varietà") },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = templateMenuExpanded)
                    },
                )
                ExposedDropdownMenu(
                    expanded = templateMenuExpanded,
                    onDismissRequest = { templateMenuExpanded = false },
                ) {
                    if (templates.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("Nessuna varietà: toccami per crearne una") },
                            onClick = {
                                templateMenuExpanded = false
                                onManageVarieties()
                            },
                        )
                    }
                    templates.forEach { template ->
                        DropdownMenuItem(
                            text = { Text(template.name) },
                            onClick = {
                                viewModel.onSelectTemplate(template)
                                templateMenuExpanded = false
                            },
                        )
                    }
                }
            }

            Spacer(Modifier.height(Spacing.sm))
            OutlinedTextField(
                value = viewModel.trayName,
                onValueChange = viewModel::onNameChange,
                label = { Text("Nome vassoio") },
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(Spacing.sm))
            DatePickerField(
                label = "Data di semina",
                date = viewModel.sowingDate,
                onDateChange = viewModel::onSowingDateChange,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(Spacing.sm))
            OutlinedTextField(
                value = viewModel.seedQuantityText,
                onValueChange = viewModel::onSeedQuantityChange,
                label = { Text("Quantità semi iniziale (g)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(Spacing.sm))
            ExposedDropdownMenuBox(
                expanded = substrateMenuExpanded,
                onExpandedChange = { substrateMenuExpanded = it },
            ) {
                OutlinedTextField(
                    value = viewModel.substrateType.displayLabel(),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Substrato") },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = substrateMenuExpanded)
                    },
                )
                ExposedDropdownMenu(
                    expanded = substrateMenuExpanded,
                    onDismissRequest = { substrateMenuExpanded = false },
                ) {
                    SubstrateType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.displayLabel()) },
                            onClick = {
                                viewModel.onSubstrateTypeChange(type)
                                substrateMenuExpanded = false
                            },
                        )
                    }
                }
            }

            Spacer(Modifier.height(Spacing.sm))
            OutlinedTextField(
                value = viewModel.substrateNotes,
                onValueChange = viewModel::onSubstrateNotesChange,
                label = { Text("Note substrato") },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
