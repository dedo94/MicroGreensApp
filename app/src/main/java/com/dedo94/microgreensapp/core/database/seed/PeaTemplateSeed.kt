package com.dedo94.microgreensapp.core.database.seed

import com.dedo94.microgreensapp.core.database.dao.TemplatePhaseDao
import com.dedo94.microgreensapp.core.database.dao.TemplateStepDao
import com.dedo94.microgreensapp.core.database.dao.VarietyTemplateDao
import com.dedo94.microgreensapp.core.database.entity.ActionType
import com.dedo94.microgreensapp.core.database.entity.TemplatePhaseEntity
import com.dedo94.microgreensapp.core.database.entity.TemplateStepEntity
import com.dedo94.microgreensapp.core.database.entity.VarietyTemplateEntity
import java.time.LocalTime

/**
 * Template precaricato per i piselli. La conservazione è la stessa del
 * girasole; il resto (ammollo, germinazione nel vassoio, crescita, raccolta)
 * segue tempi propri, diversi dal girasole. Completamente modificabile da
 * "Gestisci varietà" una volta creato.
 *
 * A differenza del girasole, trasferimento nel vassoio e prevenzione muffa
 * iniziano lo stesso giorno: sono nella stessa fase ("Trasferimento e
 * germinazione") per riflettere che avvengono in parallelo.
 */
object PeaTemplateSeed {

    private const val PLANT_TYPE = "Pisum Sativum"

    suspend fun seedIfNeeded(
        templateDao: VarietyTemplateDao,
        phaseDao: TemplatePhaseDao,
        stepDao: TemplateStepDao,
    ) {
        if (templateDao.getByName("Piselli") != null) return

        val templateId = templateDao.insert(
            VarietyTemplateEntity(
                name = "Piselli",
                plantType = PLANT_TYPE,
                notes = "Coltivazione idroponica dei microgreens di piselli.",
            )
        )

        val ammolloPhaseId = phaseDao.insert(
            TemplatePhaseEntity(templateId = templateId, orderIndex = 0, name = "Ammollo", durationDays = 1)
        )
        stepDao.insert(
            TemplateStepEntity(
                phaseId = ammolloPhaseId,
                orderIndex = 0,
                name = "Ammollo",
                actionType = ActionType.SOAKING,
                offsetStartDays = 0,
                offsetEndDays = 0,
                durationHours = 10,
                reminderTimes = listOf(LocalTime.of(8, 0)),
                instructions = "10 ore di ammollo. I semi devono stare completamente sommersi.",
            )
        )

        val trasferimentoPhaseId = phaseDao.insert(
            TemplatePhaseEntity(
                templateId = templateId,
                orderIndex = 1,
                name = "Trasferimento e germinazione",
                durationDays = 3,
            )
        )
        stepDao.insertAll(
            listOf(
                TemplateStepEntity(
                    phaseId = trasferimentoPhaseId,
                    orderIndex = 0,
                    name = "Trasferimento nel vassoio",
                    actionType = ActionType.TRAY_TRANSFER,
                    offsetStartDays = 0,
                    offsetEndDays = 0,
                    reminderTimes = listOf(LocalTime.of(8, 0)),
                    instructions = "Mettere i semi nel vassoio assicurandosi che non si sovrappongano. " +
                        "Un peso sopra potrebbe aiutare la crescita.",
                ),
                TemplateStepEntity(
                    phaseId = trasferimentoPhaseId,
                    orderIndex = 1,
                    name = "Prevenzione muffa",
                    actionType = ActionType.MOLD_PREVENTION,
                    offsetStartDays = 0,
                    offsetEndDays = 2,
                    reminderTimes = listOf(LocalTime.of(8, 0), LocalTime.of(20, 0)),
                    instructions = "Sciacquare i semi sotto acqua corrente 2 volte al giorno per " +
                        "prevenire la muffa.",
                ),
            )
        )

        val crescitaPhaseId = phaseDao.insert(
            TemplatePhaseEntity(templateId = templateId, orderIndex = 2, name = "Crescita", durationDays = 5)
        )
        stepDao.insert(
            TemplateStepEntity(
                phaseId = crescitaPhaseId,
                orderIndex = 0,
                name = "Crescita",
                actionType = ActionType.LIGHT_GROWTH,
                offsetStartDays = 0,
                offsetEndDays = 4,
                reminderTimes = listOf(LocalTime.of(8, 0)),
                instructions = "Se iniziano a crescere le radici sotto, esporre le piante alla " +
                    "luce indiretta per almeno 6 ore. Annaffiare da sotto.",
            )
        )

        val raccoltoPhaseId = phaseDao.insert(
            TemplatePhaseEntity(templateId = templateId, orderIndex = 3, name = "Raccolto", durationDays = null)
        )
        stepDao.insertAll(
            listOf(
                TemplateStepEntity(
                    phaseId = raccoltoPhaseId,
                    orderIndex = 0,
                    name = "Raccolta",
                    actionType = ActionType.HARVEST,
                    offsetStartDays = 0,
                    offsetEndDays = 2,
                    reminderTimes = listOf(LocalTime.of(8, 0)),
                    instructions = "La crescita dura 9-11 giorni: raccogliere quando i germogli " +
                        "raggiungono circa 20cm di altezza. Tagliare il più vicino alla radice possibile.",
                ),
                TemplateStepEntity(
                    phaseId = raccoltoPhaseId,
                    orderIndex = 1,
                    name = "Conservazione",
                    actionType = ActionType.STORAGE,
                    offsetStartDays = 0,
                    offsetEndDays = null,
                    reminderTimes = emptyList(),
                    instructions = "Mantenuti in frigo in una busta sigillata durano una decina di giorni.",
                ),
            )
        )
    }
}
