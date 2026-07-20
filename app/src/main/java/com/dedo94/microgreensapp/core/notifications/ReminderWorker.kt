package com.dedo94.microgreensapp.core.notifications

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

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

        val phaseName = inputData.getString(KEY_PHASE_NAME)
        // La fase si mostra solo se aggiunge un'informazione, non se
        // ripeterebbe lo stesso testo del nome step (es. fase "Ammollo"
        // con l'unico step al suo interno chiamato anch'esso "Ammollo").
        val text = if (phaseName != null && !phaseName.equals(stepName, ignoreCase = true)) {
            "$phaseName · $stepName"
        } else {
            stepName
        }

        NotificationHelper.showReminder(
            context = applicationContext,
            trayId = trayId,
            title = trayName,
            text = text,
        )
        return Result.success()
    }

    companion object {
        const val KEY_TRAY_ID = "trayId"
        const val KEY_TRAY_NAME = "trayName"
        const val KEY_STEP_NAME = "stepName"
        const val KEY_PHASE_NAME = "phaseName"
    }
}
