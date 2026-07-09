package com.dedo94.microgreensapp.core.notifications

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.dedo94.microgreensapp.core.database.entity.ActionType
import com.dedo94.microgreensapp.ui.displayLabel

/**
 * Costruisce e mostra un singolo promemoria. Non ha dipendenze iniettate:
 * tutti i dati necessari arrivano già pronti tramite l'input data, impostato
 * al momento della schedulazione da [NotificationScheduler]. Questo evita di
 * dover collegare un WorkerFactory personalizzato (Hilt) solo per un worker
 * che non ha bisogno di accedere al database.
 */
class ReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        val trayId = inputData.getLong(KEY_TRAY_ID, -1L)
        val trayName = inputData.getString(KEY_TRAY_NAME)
        val stepName = inputData.getString(KEY_STEP_NAME) ?: ""
        if (trayId == -1L || trayName == null) return Result.failure()

        val actionType = inputData.getString(KEY_ACTION_TYPE)
            ?.let { runCatching { ActionType.valueOf(it) }.getOrNull() }
            ?: ActionType.CUSTOM

        NotificationHelper.showReminder(
            context = applicationContext,
            trayId = trayId,
            title = trayName,
            text = "${actionType.displayLabel()}: $stepName",
        )
        return Result.success()
    }

    companion object {
        const val KEY_TRAY_ID = "trayId"
        const val KEY_TRAY_NAME = "trayName"
        const val KEY_STEP_NAME = "stepName"
        const val KEY_ACTION_TYPE = "actionType"
    }
}
