package com.dedo94.microgreensapp.core.repository

import com.dedo94.microgreensapp.core.database.dao.EventDao
import com.dedo94.microgreensapp.core.database.dao.TemplateStepDao
import com.dedo94.microgreensapp.core.database.dao.TrayDao
import com.dedo94.microgreensapp.core.database.dao.TrayStepDao
import com.dedo94.microgreensapp.core.database.entity.EventEntity
import com.dedo94.microgreensapp.core.database.entity.SubstrateType
import com.dedo94.microgreensapp.core.database.entity.TrayEntity
import com.dedo94.microgreensapp.core.database.entity.TrayStatus
import com.dedo94.microgreensapp.core.database.entity.TrayStepEntity
import com.dedo94.microgreensapp.core.database.entity.TrayStepStatus
import com.dedo94.microgreensapp.core.notifications.NotificationScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrayRepository @Inject constructor(
    private val trayDao: TrayDao,
    private val trayStepDao: TrayStepDao,
    private val eventDao: EventDao,
    private val templateStepDao: TemplateStepDao,
    private val notificationScheduler: NotificationScheduler,
) {
    fun observeTrays(): Flow<List<TrayEntity>> = trayDao.getAll()

    fun observeTray(trayId: Long): Flow<TrayEntity?> = trayDao.observeById(trayId)

    fun stepsForTray(trayId: Long): Flow<List<TrayStepEntity>> = trayStepDao.getStepsForTray(trayId)

    fun eventsForTray(trayId: Long): Flow<List<EventEntity>> = eventDao.getEventsForTray(trayId)

    fun stepsOverlappingRange(start: LocalDate, end: LocalDate): Flow<List<TrayStepEntity>> =
        trayStepDao.getStepsOverlappingRange(start, end)

    fun eventsInRange(start: LocalDate, end: LocalDate): Flow<List<EventEntity>> =
        eventDao.getEventsInRange(start, end)

    /**
     * Crea un vassoio e copia (snapshot) gli step del template scelto in
     * righe [TrayStepEntity] con date assolute. Da questo momento il
     * vassoio non legge più il template: modificarlo in seguito non ha
     * alcun effetto su questo vassoio.
     *
     * Uno step del template che copre più giorni (es. "Crescita/Luce" nei
     * giorni 5-10) viene qui espanso in una riga per ciascun giorno, così
     * ogni vassoio ha un task giornaliero confermabile singolarmente invece
     * di un unico step con un'unica spunta per l'intero intervallo.
     */
    suspend fun createTray(
        name: String,
        templateId: Long,
        varietyName: String,
        sowingDate: LocalDate,
        initialSeedQuantityGrams: Double?,
        substrateType: SubstrateType,
        substrateNotes: String,
    ): Long {
        val trayId = trayDao.insert(
            TrayEntity(
                name = name,
                varietyTemplateId = templateId,
                varietyName = varietyName,
                sowingDate = sowingDate,
                initialSeedQuantityGrams = initialSeedQuantityGrams,
                substrateType = substrateType,
                substrateNotes = substrateNotes,
            )
        )
        val templateSteps = templateStepDao.getStepsForTemplateOnce(templateId)
        val traySteps = templateSteps.flatMap { step ->
            val start = sowingDate.plusDays(step.offsetStartDays.toLong())
            val end = sowingDate.plusDays((step.offsetEndDays ?: step.offsetStartDays).toLong())
            generateSequence(start) { it.plusDays(1) }
                .takeWhile { !it.isAfter(end) }
                .map { day ->
                    TrayStepEntity(
                        trayId = trayId,
                        sourceTemplateStepId = step.id,
                        orderIndex = step.orderIndex,
                        name = step.name,
                        actionType = step.actionType,
                        plannedStartDate = day,
                        plannedEndDate = day,
                        durationHours = step.durationHours,
                        repeatPerDay = step.repeatPerDay,
                        reminderTimes = step.reminderTimes,
                        instructions = step.instructions,
                    )
                }
        }
        if (traySteps.isNotEmpty()) {
            trayStepDao.insertAll(traySteps)
        }
        notificationScheduler.rescheduleForTray(trayId, name, traySteps)
        return trayId
    }

    /**
     * Aggiorna i metadati modificabili del vassoio (nome, semi, substrato).
     * Non tocca sowingDate né gli step già pianificati: cambiarla
     * richiederebbe ricalcolare le date assolute di tutti gli step ancora
     * da fare, fuori scope per ora. Ricalcola i promemoria perché il nome
     * del vassoio è incluso nel testo delle notifiche già in coda.
     */
    suspend fun updateTrayDetails(
        tray: TrayEntity,
        name: String,
        initialSeedQuantityGrams: Double?,
        substrateType: SubstrateType,
        substrateNotes: String,
    ) {
        trayDao.update(
            tray.copy(
                name = name,
                initialSeedQuantityGrams = initialSeedQuantityGrams,
                substrateType = substrateType,
                substrateNotes = substrateNotes,
            )
        )
        rescheduleReminders(tray.id)
    }

    suspend fun updateTrayStatus(tray: TrayEntity, status: TrayStatus) {
        trayDao.update(tray.copy(status = status))
        if (status == TrayStatus.IN_PROGRESS) {
            rescheduleReminders(tray.id)
        } else {
            notificationScheduler.cancelForTray(tray.id)
        }
    }

    suspend fun deleteTray(tray: TrayEntity) {
        trayDao.delete(tray)
        notificationScheduler.cancelForTray(tray.id)
    }

    suspend fun updateTrayStep(step: TrayStepEntity) {
        trayStepDao.update(step)
        rescheduleReminders(step.trayId)
    }

    suspend fun deleteTrayStep(step: TrayStepEntity) {
        trayStepDao.delete(step)
        rescheduleReminders(step.trayId)
    }

    /** Segna uno step pianificato come fatto e registra l'evento corrispondente nel diario. */
    suspend fun markStepDone(step: TrayStepEntity, quantityValue: Double? = null, quantityUnit: String = "") {
        trayStepDao.update(step.copy(status = TrayStepStatus.DONE))
        eventDao.insert(
            EventEntity(
                trayId = step.trayId,
                trayStepId = step.id,
                eventDate = step.plannedStartDate,
                eventType = step.actionType,
                title = step.name,
                quantityValue = quantityValue,
                quantityUnit = quantityUnit,
            )
        )
        rescheduleReminders(step.trayId)
    }

    suspend fun markStepSkipped(step: TrayStepEntity) {
        trayStepDao.update(step.copy(status = TrayStepStatus.SKIPPED))
        rescheduleReminders(step.trayId)
    }

    /**
     * Riporta uno step DONE/SKIPPED a PENDING, per correggere una spunta
     * data per errore. Rimuove anche l'eventuale evento di diario creato da
     * [markStepDone], altrimenti resterebbe conteggiato nelle statistiche
     * pur avendo annullato lo step.
     */
    suspend fun markStepPending(step: TrayStepEntity) {
        trayStepDao.update(step.copy(status = TrayStepStatus.PENDING))
        eventDao.deleteByTrayStepId(step.id)
        rescheduleReminders(step.trayId)
    }

    private suspend fun rescheduleReminders(trayId: Long) {
        val trayName = trayDao.observeById(trayId).firstOrNull()?.name ?: return
        val steps = trayStepDao.getStepsForTrayOnce(trayId)
        notificationScheduler.rescheduleForTray(trayId, trayName, steps)
    }

    suspend fun getEvent(eventId: Long): EventEntity? = eventDao.getById(eventId)

    suspend fun addEvent(event: EventEntity): Long = eventDao.insert(event)

    suspend fun updateEvent(event: EventEntity) = eventDao.update(event)

    suspend fun deleteEvent(event: EventEntity) = eventDao.delete(event)
}
