package com.dedo94.microgreensapp.feature.tray

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dedo94.microgreensapp.core.database.entity.TrayEntity
import com.dedo94.microgreensapp.core.repository.StatsRepository
import com.dedo94.microgreensapp.core.repository.TrayRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class TrayListItemUiState(
    val tray: TrayEntity,
    val daysSinceSowing: Long,
    val plannedCycleDays: Long?,
    val progress: Float?,
    val harvestTotalGrams: Double?,
    val actualCycleDays: Long?,
)

@HiltViewModel
class TraysListViewModel @Inject constructor(
    repository: TrayRepository,
    statsRepository: StatsRepository,
) : ViewModel() {

    val trays: StateFlow<List<TrayListItemUiState>> =
        combine(repository.observeTrays(), statsRepository.observeOverview()) { trays, overview ->
            val statsByTrayId = overview.trayStats.associateBy { it.tray.id }
            trays.map { tray ->
                val daysSinceSowing = ChronoUnit.DAYS.between(tray.sowingDate, LocalDate.now())
                val stats = statsByTrayId[tray.id]
                val plannedCycleDays = stats?.plannedCycleDays
                val progress = plannedCycleDays
                    ?.takeIf { it > 0 }
                    ?.let { (daysSinceSowing.toFloat() / it.toFloat()).coerceIn(0f, 1f) }
                TrayListItemUiState(
                    tray = tray,
                    daysSinceSowing = daysSinceSowing,
                    plannedCycleDays = plannedCycleDays,
                    progress = progress,
                    harvestTotalGrams = stats?.harvestTotalGrams,
                    actualCycleDays = stats?.actualCycleDays,
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
