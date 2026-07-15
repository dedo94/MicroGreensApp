package com.dedo94.microgreensapp.core.repository

import com.dedo94.microgreensapp.core.database.dao.EventDao
import com.dedo94.microgreensapp.core.database.dao.TrayDao
import com.dedo94.microgreensapp.core.database.dao.TrayStepDao
import com.dedo94.microgreensapp.core.database.entity.ActionType
import com.dedo94.microgreensapp.core.database.entity.EventEntity
import com.dedo94.microgreensapp.core.database.entity.TrayEntity
import com.dedo94.microgreensapp.core.database.entity.TrayStatus
import com.dedo94.microgreensapp.core.database.entity.TrayStepStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

data class TrayStats(
    val tray: TrayEntity,
    /** Null se nessun evento di irrigazione ha una quantità: in pratica quasi sempre, essendo l'acqua difficile da misurare (nebulizzazione/sotto-vassoio). */
    val waterTotalMl: Double?,
    val harvestTotalGrams: Double?,
    val yieldPerSeedGram: Double?,
    val waterPerHarvestGram: Double?,
    val actualCycleDays: Long?,
    val plannedCycleDays: Long?,
    val avgTemperature: Double?,
    val avgHumidity: Double?,
    val stepsDone: Int,
    val stepsSkipped: Int,
    /** % di step non più pendenti fatti come da piano; null se nessuno step è stato ancora fatto/saltato. */
    val adherencePercent: Double?,
)

data class VarietyStats(
    val varietyName: String,
    val cycleCount: Int,
    val harvestedCount: Int,
    val avgHarvestGrams: Double?,
    val avgCycleDays: Double?,
    val avgYieldPerSeedGram: Double?,
    val yieldSampleCount: Int,
)

/**
 * Un punto della produzione per periodo: grammi raccolti nel periodo e semi
 * usati dai vassoi il cui ultimo raccolto cade in quel periodo (non i semi
 * seminati nel periodo: seminare e raccogliere avvengono quasi sempre in
 * momenti diversi, questo tiene i due numeri riferiti allo stesso ciclo).
 */
data class MonthlyProductionPoint(
    val month: YearMonth,
    val harvestGrams: Double,
    val seedGrams: Double,
)

data class YearlyProductionPoint(
    val year: Int,
    val harvestGrams: Double,
    val seedGrams: Double,
)

/** KPI/record/produzione calcolati su un insieme di vassoi: globale, oppure ristretto a una singola varietà. */
data class StatsSummary(
    val monthlyProduction: List<MonthlyProductionPoint>,
    val yearlyProduction: List<YearlyProductionPoint>,
    val bestYieldTray: TrayStats?,
    val bestYieldRatioTray: TrayStats?,
    val shortestCycleTray: TrayStats?,
    val activeTrayCount: Int,
    val last30DaysHarvestGrams: Double,
    val avgYieldPerSeedGram: Double?,
)

data class StatsOverview(
    val trayStats: List<TrayStats>,
    val varietyStats: List<VarietyStats>,
    val overallSummary: StatsSummary,
    /** Stesso calcolo di [overallSummary], ristretto ai soli vassoi di ciascuna varietà. */
    val summaryByVariety: Map<String, StatsSummary>,
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

                val waterValues = trayEvents
                    .filter { it.eventType == ActionType.WATERING }
                    .mapNotNull { it.quantityValue }
                val waterTotal = waterValues.takeIf { it.isNotEmpty() }?.sum()

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
                val waterPerHarvestGram = if (waterTotal != null && harvestTotal != null && harvestTotal > 0) {
                    waterTotal / harvestTotal
                } else null

                val temperatures = trayEvents.mapNotNull { it.actualTemperature }
                val humidities = trayEvents.mapNotNull { it.actualHumidity }

                val traySteps = stepsByTray[tray.id].orEmpty()
                val stepsDone = traySteps.count { it.status == TrayStepStatus.DONE }
                val stepsSkipped = traySteps.count { it.status == TrayStepStatus.SKIPPED }
                val adherencePercent = (stepsDone + stepsSkipped).takeIf { it > 0 }
                    ?.let { stepsDone.toDouble() / it * 100 }

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
                    stepsDone = stepsDone,
                    stepsSkipped = stepsSkipped,
                    adherencePercent = adherencePercent,
                )
            }

            val trayStatsByVariety = trayStats.groupBy { it.tray.varietyName }

            val varietyStats = trayStatsByVariety
                .map { (varietyName, statsForVariety) ->
                    val harvestValues = statsForVariety.mapNotNull { it.harvestTotalGrams }
                    val cycleValues = statsForVariety.mapNotNull { it.actualCycleDays }
                    val yieldRatioValues = statsForVariety.mapNotNull { it.yieldPerSeedGram }
                    VarietyStats(
                        varietyName = varietyName,
                        cycleCount = statsForVariety.size,
                        harvestedCount = statsForVariety.count { it.tray.status == TrayStatus.HARVESTED },
                        avgHarvestGrams = harvestValues.takeIf { it.isNotEmpty() }?.average(),
                        avgCycleDays = cycleValues.takeIf { it.isNotEmpty() }
                            ?.map { it.toDouble() }
                            ?.average(),
                        avgYieldPerSeedGram = yieldRatioValues.takeIf { it.isNotEmpty() }?.average(),
                        yieldSampleCount = yieldRatioValues.size,
                    )
                }
                .sortedBy { it.varietyName }

            StatsOverview(
                trayStats = trayStats.sortedByDescending { it.tray.sowingDate },
                varietyStats = varietyStats,
                overallSummary = buildSummary(trayStats, eventsByTray),
                summaryByVariety = trayStatsByVariety
                    .mapValues { (_, statsForVariety) -> buildSummary(statsForVariety, eventsByTray) },
            )
        }

    private fun buildSummary(
        trayStats: List<TrayStats>,
        eventsByTray: Map<Long, List<EventEntity>>,
    ): StatsSummary {
        val harvestEvents = trayStats.flatMap { eventsByTray[it.tray.id].orEmpty() }
            .filter { it.eventType == ActionType.HARVEST }

        val harvestGramsByMonth = harvestEvents
            .mapNotNull { event -> event.quantityValue?.let { YearMonth.from(event.eventDate) to it } }
            .groupBy({ it.first }, { it.second })
            .mapValues { (_, values) -> values.sum() }
        val harvestGramsByYear = harvestEvents
            .mapNotNull { event -> event.quantityValue?.let { event.eventDate.year to it } }
            .groupBy({ it.first }, { it.second })
            .mapValues { (_, values) -> values.sum() }

        // Semi del vassoio attribuiti alla data del suo ultimo raccolto (non
        // alla semina): tiene lo stesso ciclo di raccolto+semi nello stesso periodo.
        val seedGramsByTrayHarvestDate = trayStats.mapNotNull { stat ->
            val seedGrams = stat.tray.initialSeedQuantityGrams ?: return@mapNotNull null
            val lastHarvestDate = eventsByTray[stat.tray.id].orEmpty()
                .filter { it.eventType == ActionType.HARVEST }
                .maxOfOrNull { it.eventDate } ?: return@mapNotNull null
            lastHarvestDate to seedGrams
        }
        val seedGramsByMonth = seedGramsByTrayHarvestDate
            .groupBy({ YearMonth.from(it.first) }, { it.second })
            .mapValues { (_, values) -> values.sum() }
        val seedGramsByYear = seedGramsByTrayHarvestDate
            .groupBy({ it.first.year }, { it.second })
            .mapValues { (_, values) -> values.sum() }

        val monthlyProduction = (harvestGramsByMonth.keys + seedGramsByMonth.keys)
            .distinct()
            .sorted()
            .map { month ->
                MonthlyProductionPoint(
                    month = month,
                    harvestGrams = harvestGramsByMonth[month] ?: 0.0,
                    seedGrams = seedGramsByMonth[month] ?: 0.0,
                )
            }
        val yearlyProduction = (harvestGramsByYear.keys + seedGramsByYear.keys)
            .distinct()
            .sorted()
            .map { year ->
                YearlyProductionPoint(
                    year = year,
                    harvestGrams = harvestGramsByYear[year] ?: 0.0,
                    seedGrams = seedGramsByYear[year] ?: 0.0,
                )
            }

        val last30DaysStart = LocalDate.now().minusDays(30)
        val last30DaysHarvest = harvestEvents
            .filter { !it.eventDate.isBefore(last30DaysStart) }
            .sumOf { it.quantityValue ?: 0.0 }

        val yieldRatios = trayStats.mapNotNull { it.yieldPerSeedGram }

        return StatsSummary(
            monthlyProduction = monthlyProduction,
            yearlyProduction = yearlyProduction,
            bestYieldTray = trayStats.filter { it.harvestTotalGrams != null }
                .maxByOrNull { it.harvestTotalGrams!! },
            bestYieldRatioTray = trayStats.filter { it.yieldPerSeedGram != null }
                .maxByOrNull { it.yieldPerSeedGram!! },
            shortestCycleTray = trayStats.filter { it.actualCycleDays != null }
                .minByOrNull { it.actualCycleDays!! },
            activeTrayCount = trayStats.count { it.tray.status == TrayStatus.IN_PROGRESS },
            last30DaysHarvestGrams = last30DaysHarvest,
            avgYieldPerSeedGram = yieldRatios.takeIf { it.isNotEmpty() }?.average(),
        )
    }
}
