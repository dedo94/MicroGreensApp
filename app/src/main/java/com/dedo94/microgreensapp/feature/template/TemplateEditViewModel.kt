package com.dedo94.microgreensapp.feature.template

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.dedo94.microgreensapp.core.database.entity.TemplateStepEntity
import com.dedo94.microgreensapp.core.repository.TemplateRepository
import com.dedo94.microgreensapp.navigation.TemplateEditRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TemplateEditViewModel @Inject constructor(
    private val repository: TemplateRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val route: TemplateEditRoute = savedStateHandle.toRoute()
    val isNew: Boolean = route.templateId == 0L

    private val _templateId = MutableStateFlow(route.templateId.takeIf { it != 0L })
    val templateId: StateFlow<Long?> = _templateId.asStateFlow()

    var name by mutableStateOf("")
        private set
    var plantType by mutableStateOf("")
        private set
    var notes by mutableStateOf("")
        private set
    var isInfoSaved by mutableStateOf(!isNew)
        private set

    val steps: StateFlow<List<TemplateStepEntity>> = _templateId
        .flatMapLatest { id -> if (id == null) flowOf(emptyList()) else repository.stepsForTemplate(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        if (!isNew) {
            viewModelScope.launch {
                repository.observeTemplate(route.templateId).firstOrNull()?.let { template ->
                    name = template.name
                    plantType = template.plantType
                    notes = template.notes
                }
            }
        }
    }

    fun onNameChange(value: String) {
        name = value
        isInfoSaved = false
    }

    fun onPlantTypeChange(value: String) {
        plantType = value
        isInfoSaved = false
    }

    fun onNotesChange(value: String) {
        notes = value
        isInfoSaved = false
    }

    fun saveTemplateInfo() {
        if (name.isBlank()) return
        viewModelScope.launch {
            val id = _templateId.value
            if (id == null) {
                _templateId.value = repository.createTemplate(name, plantType, notes)
            } else {
                repository.observeTemplate(id).firstOrNull()?.let { existing ->
                    repository.updateTemplate(
                        existing.copy(name = name, plantType = plantType, notes = notes)
                    )
                }
            }
            isInfoSaved = true
        }
    }

    fun addStep(step: TemplateStepEntity) {
        val id = _templateId.value ?: return
        viewModelScope.launch {
            val orderIndex = repository.nextOrderIndex(id)
            repository.addStep(step.copy(templateId = id, orderIndex = orderIndex))
        }
    }

    fun updateStep(step: TemplateStepEntity) {
        viewModelScope.launch { repository.updateStep(step) }
    }

    fun deleteStep(step: TemplateStepEntity) {
        viewModelScope.launch { repository.deleteStep(step) }
    }

    fun moveStep(from: Int, to: Int) {
        val current = steps.value.toMutableList()
        if (from !in current.indices || to !in current.indices) return
        val item = current.removeAt(from)
        current.add(to, item)
        viewModelScope.launch { repository.reorderSteps(current) }
    }

    fun deleteTemplate(onDeleted: () -> Unit) {
        val id = _templateId.value ?: return
        viewModelScope.launch {
            repository.observeTemplate(id).firstOrNull()?.let { repository.deleteTemplate(it) }
            onDeleted()
        }
    }
}
