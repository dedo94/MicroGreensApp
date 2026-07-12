package com.dedo94.microgreensapp.feature.tray

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.dedo94.microgreensapp.core.database.entity.EventEntity
import com.dedo94.microgreensapp.core.database.entity.TrayEntity
import com.dedo94.microgreensapp.core.database.entity.TrayStatus
import com.dedo94.microgreensapp.core.database.entity.TrayStepEntity
import com.dedo94.microgreensapp.core.repository.StatsRepository
import com.dedo94.microgreensapp.core.repository.TrayRepository
import com.dedo94.microgreensapp.core.repository.TrayStats
import com.dedo94.microgreensapp.navigation.TrayDetailRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HarvestPrediction(val predictedGrams: Double, val basedOnCycles: Int)

@HiltViewModel
class TrayDetailViewModel @Inject constructor(
    private val repository: TrayRepository,
    statsRepository: StatsRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val route: TrayDetailRoute = savedStateHandle.toRoute()
    val trayId: Long = route.trayId

    val tray: StateFlow<TrayEntity?> = repository.observeTray(trayId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /**
     * Semi iniziali di questo vassoio moltiplicati per la resa media per
     * grammo di seme registrata sui cicli già raccolti della stessa
     * varietà. Null finché non c'è almeno uno storico su cui basarsi.
     */
    val harvestPrediction: StateFlow<HarvestPrediction?> =
        combine(tray, statsRepository.observeOverview()) { currentTray, overview ->
            val seedQuantity = currentTray?.initialSeedQuantityGrams
            if (currentTray == null || seedQuantity == null || seedQuantity <= 0) return@combine null
            val varietyStats = overview.varietyStats.find { it.varietyName == currentTray.varietyName }
            val avgRatio = varietyStats?.avgYieldPerSeedGram
            if (avgRatio == null || varietyStats.yieldSampleCount == 0) {
                null
            } else {
                HarvestPrediction(seedQuantity * avgRatio, varietyStats.yieldSampleCount)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Statistiche del vassoio corrente (aderenza al piano, resa, ecc.), null finché non calcolate. */
    val trayStats: StateFlow<TrayStats?> =
        combine(tray, statsRepository.observeOverview()) { currentTray, overview ->
            currentTray?.let { t -> overview.trayStats.find { it.tray.id == t.id } }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val steps: StateFlow<List<TrayStepEntity>> = repository.stepsForTray(trayId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val events: StateFlow<List<EventEntity>> = repository.eventsForTray(trayId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun markDone(step: TrayStepEntity, quantityValue: Double? = null, quantityUnit: String = "") {
        viewModelScope.launch { repository.markStepDone(step, quantityValue, quantityUnit) }
    }

    fun markSkipped(step: TrayStepEntity) {
        viewModelScope.launch { repository.markStepSkipped(step) }
    }

    fun markPending(step: TrayStepEntity) {
        viewModelScope.launch { repository.markStepPending(step) }
    }

    fun updateStep(step: TrayStepEntity) {
        viewModelScope.launch { repository.updateTrayStep(step) }
    }

    fun deleteStep(step: TrayStepEntity) {
        viewModelScope.launch { repository.deleteTrayStep(step) }
    }

    fun deleteEvent(event: EventEntity) {
        viewModelScope.launch { repository.deleteEvent(event) }
    }

    fun setStatus(status: TrayStatus) {
        val current = tray.value ?: return
        viewModelScope.launch { repository.updateTrayStatus(current, status) }
    }

    fun deleteTray(onDeleted: () -> Unit) {
        val current = tray.value ?: return
        viewModelScope.launch {
            repository.deleteTray(current)
            onDeleted()
        }
    }
}
