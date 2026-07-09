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
        val traySteps = templateSteps.map { step ->
            TrayStepEntity(
                trayId = trayId,
                sourceTemplateStepId = step.id,
                orderIndex = step.orderIndex,
                name = step.name,
                actionType = step.actionType,
                plannedStartDate = sowingDate.plusDays(step.offsetStartDays.toLong()),
                plannedEndDate = sowingDate.plusDays(
                    (step.offsetEndDays ?: step.offsetStartDays).toLong()
                ),
                durationHours = step.durationHours,
                repeatPerDay = step.repeatPerDay,
                reminderTimes = step.reminderTimes,
                instructions = step.instructions,
            )
        }
        if (traySteps.isNotEmpty()) {
            trayStepDao.insertAll(traySteps)
        }
        notificationScheduler.rescheduleForTray(trayId, name, traySteps)
        return trayId
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
                eventDate = LocalDate.now(),
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
