package com.dedo94.microgreensapp.core.database.seed

import com.dedo94.microgreensapp.core.database.dao.TemplateStepDao
import com.dedo94.microgreensapp.core.database.dao.VarietyTemplateDao
import com.dedo94.microgreensapp.core.database.entity.ActionType
import com.dedo94.microgreensapp.core.database.entity.TemplateStepEntity
import com.dedo94.microgreensapp.core.database.entity.VarietyTemplateEntity
import java.time.LocalTime

/**
 * Template precaricato per i piselli. La conservazione è la stessa del
 * girasole; il resto (ammollo, germinazione nel vassoio, crescita, raccolta)
 * segue tempi propri, diversi dal girasole. Completamente modificabile da
 * "Gestisci varietà" una volta creato.
 */
object PeaTemplateSeed {

    private const val PLANT_TYPE = "Pisum Sativum"

    suspend fun seedIfNeeded(templateDao: VarietyTemplateDao, stepDao: TemplateStepDao) {
        val existing = templateDao.getByName("Piselli")
        val templateId = if (existing != null) {
            // I primi step precaricati avevano tempi sbagliati (12h di
            // ammollo, trasferimento nel vassoio dopo la prevenzione muffa
            // invece che prima). Corretto una tantum solo se sono ancora
            // quelli di default, per non cancellare eventuali modifiche
            // fatte dall'utente da "Gestisci varietà". I vassoi già creati
            // da questo template non sono comunque mai influenzati, perché
            // hanno il proprio piano copiato (snapshot) alla creazione.
            val currentSteps = stepDao.getStepsForTemplateOnce(existing.id)
            val isOldUncorrectedSeed = currentSteps.any { it.name == "Ammollo" && it.durationHours == 12 }
            if (!isOldUncorrectedSeed) return
            currentSteps.forEach { stepDao.delete(it) }
            existing.id
        } else {
            templateDao.insert(
                VarietyTemplateEntity(
                    name = "Piselli",
                    plantType = PLANT_TYPE,
                    notes = "Coltivazione idroponica dei microgreens di piselli.",
                )
            )
        }

        val steps = listOf(
            TemplateStepEntity(
                templateId = templateId,
                orderIndex = 0,
                name = "Ammollo",
                actionType = ActionType.SOAKING,
                offsetStartDays = 0,
                offsetEndDays = 0,
                durationHours = 10,
                repeatPerDay = 1,
                reminderTimes = listOf(LocalTime.of(8, 0)),
                instructions = "10 ore di ammollo. I semi devono stare completamente sommersi.",
            ),
            TemplateStepEntity(
                templateId = templateId,
                orderIndex = 1,
                name = "Trasferimento nel vassoio",
                actionType = ActionType.TRAY_TRANSFER,
                offsetStartDays = 1,
                offsetEndDays = 1,
                repeatPerDay = 1,
                reminderTimes = listOf(LocalTime.of(8, 0)),
                instructions = "Mettere i semi nel vassoio assicurandosi che non si sovrappongano. " +
                    "Un peso sopra potrebbe aiutare la crescita.",
            ),
            TemplateStepEntity(
                templateId = templateId,
                orderIndex = 2,
                name = "Prevenzione muffa",
                actionType = ActionType.MOLD_PREVENTION,
                offsetStartDays = 1,
                offsetEndDays = 3,
                repeatPerDay = 2,
                reminderTimes = listOf(LocalTime.of(8, 0), LocalTime.of(20, 0)),
                instructions = "Sciacquare i semi sotto acqua corrente 2 volte al giorno per " +
                    "prevenire la muffa.",
            ),
            TemplateStepEntity(
                templateId = templateId,
                orderIndex = 3,
                name = "Crescita",
                actionType = ActionType.LIGHT_GROWTH,
                offsetStartDays = 4,
                offsetEndDays = 8,
                repeatPerDay = 1,
                reminderTimes = listOf(LocalTime.of(8, 0)),
                instructions = "Se iniziano a crescere le radici sotto, esporre le piante alla " +
                    "luce indiretta per almeno 6 ore. Annaffiare da sotto.",
            ),
            TemplateStepEntity(
                templateId = templateId,
                orderIndex = 4,
                name = "Raccolta",
                actionType = ActionType.HARVEST,
                offsetStartDays = 9,
                offsetEndDays = 11,
                repeatPerDay = 1,
                reminderTimes = listOf(LocalTime.of(8, 0)),
                instructions = "La crescita dura 9-11 giorni: raccogliere quando i germogli " +
                    "raggiungono circa 20cm di altezza. Tagliare il più vicino alla radice possibile.",
            ),
            TemplateStepEntity(
                templateId = templateId,
                orderIndex = 5,
                name = "Conservazione",
                actionType = ActionType.STORAGE,
                offsetStartDays = 9,
                offsetEndDays = null,
                repeatPerDay = 1,
                reminderTimes = emptyList(),
                instructions = "Mantenuti in frigo in una busta sigillata durano una decina di giorni.",
            ),
        )

        stepDao.insertAll(steps)
    }
}
