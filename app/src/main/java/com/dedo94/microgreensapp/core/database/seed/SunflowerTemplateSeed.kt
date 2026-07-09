package com.dedo94.microgreensapp.core.database.seed

import com.dedo94.microgreensapp.core.database.dao.TemplateStepDao
import com.dedo94.microgreensapp.core.database.dao.VarietyTemplateDao
import com.dedo94.microgreensapp.core.database.entity.ActionType
import com.dedo94.microgreensapp.core.database.entity.TemplateStepEntity
import com.dedo94.microgreensapp.core.database.entity.VarietyTemplateEntity
import java.time.LocalTime

/**
 * Template precaricato al primo avvio, ricalcato esattamente sull'appunto
 * di coltivazione idroponica dei microgreens di girasole fornito dall'utente.
 */
object SunflowerTemplateSeed {

    suspend fun seedIfNeeded(templateDao: VarietyTemplateDao, stepDao: TemplateStepDao) {
        if (templateDao.countByName("Girasole") > 0) return

        val templateId = templateDao.insert(
            VarietyTemplateEntity(
                name = "Girasole",
                plantType = "Girasole (microgreens)",
                notes = "Coltivazione idroponica dei microgreens di girasole.",
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
                durationHours = 8,
                repeatPerDay = 1,
                reminderTimes = listOf(LocalTime.of(8, 0)),
                instructions = "8 ore di ammollo. I semi devono stare completamente sommersi.",
            ),
            TemplateStepEntity(
                templateId = templateId,
                orderIndex = 1,
                name = "Prevenzione muffa",
                actionType = ActionType.MOLD_PREVENTION,
                offsetStartDays = 1,
                offsetEndDays = 3,
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
                offsetStartDays = 4,
                offsetEndDays = 4,
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
                offsetStartDays = 5,
                offsetEndDays = 10,
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
                offsetStartDays = 10,
                offsetEndDays = 10,
                repeatPerDay = 1,
                reminderTimes = listOf(LocalTime.of(8, 0)),
                instructions = "Raccolta quando appaiono le prime vere foglie. Tagliare il più " +
                    "vicino alla radice possibile.",
            ),
            TemplateStepEntity(
                templateId = templateId,
                orderIndex = 5,
                name = "Conservazione",
                actionType = ActionType.STORAGE,
                offsetStartDays = 10,
                offsetEndDays = null,
                repeatPerDay = 1,
                reminderTimes = emptyList(),
                instructions = "Mantenuti in frigo in una busta sigillata durano una decina di giorni.",
            ),
        )

        stepDao.insertAll(steps)
    }
}
