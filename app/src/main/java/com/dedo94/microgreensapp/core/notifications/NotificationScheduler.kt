package com.dedo94.microgreensapp.core.notifications

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.dedo94.microgreensapp.core.database.entity.TrayStepEntity
import com.dedo94.microgreensapp.core.database.entity.TrayStepStatus
import com.dedo94.microgreensapp.core.repository.NotificationPreferenceRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Deriva i promemoria dagli step pianificati di un vassoio e li schedula con
 * WorkManager. Ad ogni chiamata cancella e ricrea da zero tutte le notifiche
 * future per quel vassoio: più semplice e robusto di una patch incrementale,
 * e va richiamata dopo ogni modifica al piano del vassoio (aggiunta/modifica/
 * completamento/eliminazione di uno step).
 */
@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferenceRepository: NotificationPreferenceRepository,
) {
    private val workManager: WorkManager
        get() = WorkManager.getInstance(context)

    suspend fun rescheduleForTray(trayId: Long, trayName: String, steps: List<TrayStepEntity>) {
        workManager.cancelAllWorkByTag(tagForTray(trayId))
        if (!preferenceRepository.enabled.first()) return
        val now = LocalDateTime.now()
        steps
            .filter { it.status == TrayStepStatus.PENDING && it.plannedTime != null }
            .forEach { step ->
                val fireAt = LocalDateTime.of(step.plannedDate, step.plannedTime)
                if (fireAt.isAfter(now)) {
                    enqueue(trayId, trayName, step, fireAt, now)
                }
            }
    }

    fun cancelForTray(trayId: Long) {
        workManager.cancelAllWorkByTag(tagForTray(trayId))
    }

    /** Cancella tutti i promemoria di tutti i vassoi, es. quando l'utente disattiva il toggle. */
    fun cancelAllReminders() {
        workManager.cancelAllWorkByTag(REMINDER_TAG)
    }

    private fun enqueue(
        trayId: Long,
        trayName: String,
        step: TrayStepEntity,
        fireAt: LocalDateTime,
        now: LocalDateTime,
    ) {
        val delayMillis = Duration.between(now, fireAt).toMillis()
        val data = Data.Builder()
            .putLong(ReminderWorker.KEY_TRAY_ID, trayId)
            .putString(ReminderWorker.KEY_TRAY_NAME, trayName)
            .putString(ReminderWorker.KEY_STEP_NAME, step.name)
            .putString(ReminderWorker.KEY_PHASE_NAME, step.phaseName)
            .build()
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag(tagForTray(trayId))
            .addTag(REMINDER_TAG)
            .build()
        workManager.enqueue(request)
    }

    private fun tagForTray(trayId: Long) = "tray_reminders_$trayId"

    private companion object {
        const val REMINDER_TAG = "microgreens_reminder"
    }
}
