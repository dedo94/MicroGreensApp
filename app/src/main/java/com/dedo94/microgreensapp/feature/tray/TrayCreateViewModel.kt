package com.dedo94.microgreensapp.feature.tray

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dedo94.microgreensapp.core.database.entity.SubstrateType
import com.dedo94.microgreensapp.core.database.entity.VarietyTemplateEntity
import com.dedo94.microgreensapp.core.repository.TemplateRepository
import com.dedo94.microgreensapp.core.repository.TrayRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class TrayCreateViewModel @Inject constructor(
    private val trayRepository: TrayRepository,
    templateRepository: TemplateRepository,
) : ViewModel() {

    val templates: StateFlow<List<VarietyTemplateEntity>> = templateRepository.activeTemplates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    var selectedTemplate by mutableStateOf<VarietyTemplateEntity?>(null)
        private set
    var trayName by mutableStateOf("")
        private set
    var sowingDate by mutableStateOf(LocalDate.now())
        private set
    var seedQuantityText by mutableStateOf("")
        private set
    var substrateType by mutableStateOf(SubstrateType.SOIL)
        private set
    var substrateNotes by mutableStateOf("")
        private set

    val canSave: Boolean
        get() = selectedTemplate != null && trayName.isNotBlank()

    fun onSelectTemplate(template: VarietyTemplateEntity) {
        selectedTemplate = template
        if (trayName.isBlank()) trayName = template.name
    }

    fun onNameChange(value: String) {
        trayName = value
    }

    fun onSowingDateChange(date: LocalDate) {
        sowingDate = date
    }

    fun onSeedQuantityChange(value: String) {
        seedQuantityText = value.filter { it.isDigit() || it == '.' }
    }

    fun onSubstrateTypeChange(type: SubstrateType) {
        substrateType = type
    }

    fun onSubstrateNotesChange(value: String) {
        substrateNotes = value
    }

    fun save(onSaved: (Long) -> Unit) {
        val template = selectedTemplate ?: return
        if (trayName.isBlank()) return
        viewModelScope.launch {
            val id = trayRepository.createTray(
                name = trayName,
                templateId = template.id,
                varietyName = template.name,
                sowingDate = sowingDate,
                initialSeedQuantityGrams = seedQuantityText.toDoubleOrNull(),
                substrateType = substrateType,
                substrateNotes = substrateNotes,
            )
            onSaved(id)
        }
    }
}
