package com.dedo94.microgreensapp.core.database.seed

import com.dedo94.microgreensapp.core.database.dao.TemplateStepDao
import com.dedo94.microgreensapp.core.database.dao.VarietyTemplateDao
import com.dedo94.microgreensapp.core.database.entity.ActionType
import com.dedo94.microgreensapp.core.database.entity.TemplateStepEntity
import com.dedo94.microgreensapp.core.database.entity.VarietyTemplateEntity
import java.time.LocalTime

/**
 * Template precaricato per i piselli, sullo stesso schema del girasole
 * (stessa sequenza di fasi, tempi tipici per microgreens di piselli):
 * completamente modificabile da "Gestisci varietà" una volta creato.
 */
object PeaTemplateSeed {

    private const val PLANT_TYPE = "Pisum Sativum"

    suspend fun seedIfNeeded(templateDao: VarietyTemplateDao, stepDao: TemplateStepDao) {
        if (templateDao.countByName("Piselli") > 0) return

        val templateId = templateDao.insert(
            VarietyTemplateEntity(
                name = "Piselli",
                plantType = PLANT_TYPE,
                notes = "Coltivazione idroponica dei microgreens di piselli.",
            )
        )

        val steps = listOf(
            TemplateStepEntity(
                templateId = templateId,
                orderIndex = 0,
                name = "Ammollo",
                actionType = ActionType.SOAKING,
                offsetStartDays = 0,
                offsetEndDays = 0,
                durationHours = 12,
                repeatPerDay = 1,
                reminderTimes = listOf(LocalTime.of(8, 0)),
                instructions = "12 ore di ammollo. I semi devono stare completamente sommersi.",
            ),
            TemplateStepEntity(
                templateId = templateId,
                orderIndex = 1,
                name = "Prevenzione muffa",
                actionType = ActionType.MOLD_PREVENTION,
                offsetStartDays = 1,
                offsetEndDays = 2,
                repeatPerDay = 2,
                reminderTimes = listOf(LocalTime.of(8, 0), LocalTime.of(20, 0)),
                instructions = "Sciacquare i semi sotto acqua corrente 2 volte al giorno. " +
                    "Mantenere coperti con un tessuto traspirante.",
            ),
            TemplateStepEntity(
                templateId = templateId,
                orderIndex = 2,
                name = "Trasferimento nel vassoio",
                actionType = ActionType.TRAY_TRANSFER,
                offsetStartDays = 3,
                offsetEndDays = 3,
                repeatPerDay = 1,
                reminderTimes = listOf(LocalTime.of(8, 0)),
                instructions = "Mettere i semi nel vassoio per la crescita. Mantenerli coperti " +
                    "dalla luce. Il peso sopra può essere facoltativo. Mantenere i semi idratati " +
                    "con uno spruzzino.",
            ),
            TemplateStepEntity(
                templateId = templateId,
                orderIndex = 3,
                name = "Crescita",
                actionType = ActionType.LIGHT_GROWTH,
                offsetStartDays = 4,
                offsetEndDays = 9,
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
                offsetEndDays = 9,
                repeatPerDay = 1,
                reminderTimes = listOf(LocalTime.of(8, 0)),
                instructions = "Raccolta quando i germogli sono alti 7-10cm, prima che appaiano i " +
                    "viticci. Tagliare il più vicino alla radice possibile.",
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
