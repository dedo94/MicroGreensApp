package com.dedo94.microgreensapp.core.repository

import com.dedo94.microgreensapp.core.database.dao.EventDao
import com.dedo94.microgreensapp.core.database.dao.TrayDao
import com.dedo94.microgreensapp.core.database.dao.TrayStepDao
import com.dedo94.microgreensapp.core.database.entity.ActionType
import com.dedo94.microgreensapp.core.database.entity.TrayEntity
import com.dedo94.microgreensapp.core.database.entity.TrayStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

data class TrayStats(
    val tray: TrayEntity,
    val waterTotalMl: Double,
    val harvestTotalGrams: Double?,
    val yieldPerSeedGram: Double?,
    val waterPerHarvestGram: Double?,
    val actualCycleDays: Long?,
    val plannedCycleDays: Long?,
    val avgTemperature: Double?,
    val avgHumidity: Double?,
)

data class VarietyStats(
    val varietyName: String,
    val cycleCount: Int,
    val harvestedCount: Int,
    val abandonedCount: Int,
    val avgHarvestGrams: Double?,
    val avgCycleDays: Double?,
    val avgYieldPerSeedGram: Double?,
    val yieldSampleCount: Int,
)

data class StatsOverview(
    val trayStats: List<TrayStats>,
    val varietyStats: List<VarietyStats>,
    val monthlyHarvestGrams: List<Pair<YearMonth, Double>>,
    val bestYieldTray: TrayStats?,
    val bestYieldRatioTray: TrayStats?,
    val shortestCycleTray: TrayStats?,
)

/**
 * Tutte le statistiche sono calcolate in Kotlin a partire dai dati grezzi
 * (vassoi/eventi/step) invece che con query SQL aggregate: per un'app
 * personale il volume di dati è minimo, e questo evita di introdurre POJO
 * di risultato e join Room solo per una manciata di vassoi.
 */
@Singleton
class StatsRepository @Inject constructor(
    private val trayDao: TrayDao,
    private val eventDao: EventDao,
    private val trayStepDao: TrayStepDao,
) {
    fun observeOverview(): Flow<StatsOverview> =
        combine(trayDao.getAll(), eventDao.getAll(), trayStepDao.getAll()) { trays, events, steps ->
            val eventsByTray = events.groupBy { it.trayId }
            val stepsByTray = steps.groupBy { it.trayId }

            val trayStats = trays.map { tray ->
                val trayEvents = eventsByTray[tray.id].orEmpty()

                val waterTotal = trayEvents
                    .filter { it.eventType == ActionType.WATERING }
                    .sumOf { it.quantityValue ?: 0.0 }

                val harvestEvents = trayEvents.filter { it.eventType == ActionType.HARVEST }
                val harvestTotal = harvestEvents.sumOf { it.quantityValue ?: 0.0 }
                    .takeIf { harvestEvents.isNotEmpty() }

                val actualCycleDays = harvestEvents.maxOfOrNull { it.eventDate }
                    ?.let { ChronoUnit.DAYS.between(tray.sowingDate, it) }
                val plannedCycleDays = stepsByTray[tray.id].orEmpty().maxOfOrNull { it.plannedEndDate }
                    ?.let { ChronoUnit.DAYS.between(tray.sowingDate, it) }

                val seedQty = tray.initialSeedQuantityGrams
                val yieldPerSeedGram = if (seedQty != null && seedQty > 0 && harvestTotal != null) {
                    harvestTotal / seedQty
                } else null
                val waterPerHarvestGram = if (harvestTotal != null && harvestTotal > 0) {
                    waterTotal / harvestTotal
                } else null

                val temperatures = trayEvents.mapNotNull { it.actualTemperature }
                val humidities = trayEvents.mapNotNull { it.actualHumidity }

                TrayStats(
                    tray = tray,
                    waterTotalMl = waterTotal,
                    harvestTotalGrams = harvestTotal,
                    yieldPerSeedGram = yieldPerSeedGram,
                    waterPerHarvestGram = waterPerHarvestGram,
                    actualCycleDays = actualCycleDays,
                    plannedCycleDays = plannedCycleDays,
                    avgTemperature = temperatures.takeIf { it.isNotEmpty() }?.average(),
                    avgHumidity = humidities.takeIf { it.isNotEmpty() }?.average(),
                )
            }

            val varietyStats = trayStats
                .groupBy { it.tray.varietyName }
                .map { (varietyName, statsForVariety) ->
                    val harvestValues = statsForVariety.mapNotNull { it.harvestTotalGrams }
                    val cycleValues = statsForVariety.mapNotNull { it.actualCycleDays }
                    val yieldRatioValues = statsForVariety.mapNotNull { it.yieldPerSeedGram }
                    VarietyStats(
                        varietyName = varietyName,
                        cycleCount = statsForVariety.size,
                        harvestedCount = statsForVariety.count { it.tray.status == TrayStatus.HARVESTED },
                        abandonedCount = statsForVariety.count { it.tray.status == TrayStatus.ABANDONED },
                        avgHarvestGrams = harvestValues.takeIf { it.isNotEmpty() }?.average(),
                        avgCycleDays = cycleValues.takeIf { it.isNotEmpty() }
                            ?.map { it.toDouble() }
                            ?.average(),
                        avgYieldPerSeedGram = yieldRatioValues.takeIf { it.isNotEmpty() }?.average(),
                        yieldSampleCount = yieldRatioValues.size,
                    )
                }
                .sortedBy { it.varietyName }

            val monthlyHarvest = trayStats
                .flatMap { stats ->
                    eventsByTray[stats.tray.id].orEmpty()
                        .filter { it.eventType == ActionType.HARVEST }
                        .mapNotNull { event -> event.quantityValue?.let { YearMonth.from(event.eventDate) to it } }
                }
                .groupBy({ it.first }, { it.second })
                .mapValues { (_, values) -> values.sum() }
                .toSortedMap()
                .map { it.key to it.value }

            StatsOverview(
                trayStats = trayStats.sortedByDescending { it.tray.sowingDate },
                varietyStats = varietyStats,
                monthlyHarvestGrams = monthlyHarvest,
                bestYieldTray = trayStats.filter { it.harvestTotalGrams != null }
                    .maxByOrNull { it.harvestTotalGrams!! },
                bestYieldRatioTray = trayStats.filter { it.yieldPerSeedGram != null }
                    .maxByOrNull { it.yieldPerSeedGram!! },
                shortestCycleTray = trayStats.filter { it.actualCycleDays != null }
                    .minByOrNull { it.actualCycleDays!! },
            )
        }
}
