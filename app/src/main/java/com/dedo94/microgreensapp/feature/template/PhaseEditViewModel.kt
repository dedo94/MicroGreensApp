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
import com.dedo94.microgreensapp.navigation.TemplatePhaseEditRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PhaseEditViewModel @Inject constructor(
    private val repository: TemplateRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val route: TemplatePhaseEditRoute = savedStateHandle.toRoute()
    val phaseId: Long = route.phaseId

    var name by mutableStateOf("")
        private set
    var hasDuration by mutableStateOf(false)
        private set
    var durationDaysText by mutableStateOf("")
        private set
    var isInfoSaved by mutableStateOf(true)
        private set

    val steps: StateFlow<List<TemplateStepEntity>> = repository.stepsForPhase(phaseId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            repository.observePhase(phaseId).firstOrNull()?.let { phase ->
                name = phase.name
                hasDuration = phase.durationDays != null
                durationDaysText = phase.durationDays?.toString() ?: ""
            }
        }
    }

    fun onNameChange(value: String) {
        name = value
        isInfoSaved = false
    }

    fun onHasDurationChange(value: Boolean) {
        hasDuration = value
        isInfoSaved = false
    }

    fun onDurationDaysTextChange(value: String) {
        durationDaysText = value.filter(Char::isDigit)
        isInfoSaved = false
    }

    fun savePhaseInfo() {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.observePhase(phaseId).firstOrNull()?.let { existing ->
                repository.updatePhase(
                    existing.copy(
                        name = name,
                        durationDays = if (hasDuration) durationDaysText.toIntOrNull() else null,
                    )
                )
            }
            isInfoSaved = true
        }
    }

    fun addStep(step: TemplateStepEntity) {
        viewModelScope.launch {
            val orderIndex = repository.nextStepOrderIndex(phaseId)
            repository.addStep(step.copy(phaseId = phaseId, orderIndex = orderIndex))
        }
    }

    fun updateStep(step: TemplateStepEntity) {
        viewModelScope.launch { repository.updateStep(step) }
    }

    fun deleteStep(step: TemplateStepEntity) {
        viewModelScope.launch { repository.deleteStep(step) }
    }

    /**
     * L'ordine finale è già calcolato dalla UI (stato locale ottimistico
     * aggiornato durante il trascinamento): qui si persiste soltanto, una
     * volta sola a fine gesto invece che ad ogni singolo spostamento.
     */
    fun reorderSteps(newOrder: List<TemplateStepEntity>) {
        viewModelScope.launch { repository.reorderSteps(newOrder) }
    }

    fun deletePhase(onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.observePhase(phaseId).firstOrNull()?.let { repository.deletePhase(it) }
            onDeleted()
        }
    }
}
