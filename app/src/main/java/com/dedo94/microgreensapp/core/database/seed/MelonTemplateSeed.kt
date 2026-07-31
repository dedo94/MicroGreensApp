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
 * Template precaricato per il melone. Dati confermati dall'utente: ammollo
 * 10-12 ore, prevenzione muffa un paio di giorni, ciclo totale 8-9 giorni.
 * Trasferimento, durata della crescita e finestra di raccolta non erano
 * specificati: dedotti per coerenza con Girasole/Piselli in modo da
 * raggiungere esattamente 8-9 giorni totali — regolabili da "Gestisci
 * varietà" se non corrispondono all'esperienza reale.
 */
object MelonTemplateSeed {

    private const val PLANT_TYPE = "Cucumis melo"

    suspend fun seedIfNeeded(
        templateDao: VarietyTemplateDao,
        phaseDao: TemplatePhaseDao,
        stepDao: TemplateStepDao,
    ) {
        if (templateDao.getByName("Melone") != null) return

        val templateId = templateDao.insert(
            VarietyTemplateEntity(
                name = "Melone",
                plantType = PLANT_TYPE,
                notes = "Coltivazione idroponica dei microgreens di melone.",
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
                durationHours = 12,
                reminderTimes = listOf(LocalTime.of(8, 0)),
                instructions = "10-12 ore di ammollo. I semi devono stare completamente sommersi.",
            )
        )

        val germinazionePhaseId = phaseDao.insert(
            TemplatePhaseEntity(templateId = templateId, orderIndex = 1, name = "Germinazione", durationDays = 2)
        )
        stepDao.insert(
            TemplateStepEntity(
                phaseId = germinazionePhaseId,
                orderIndex = 0,
                name = "Prevenzione muffa",
                actionType = ActionType.MOLD_PREVENTION,
                offsetStartDays = 0,
                offsetEndDays = 1,
                reminderTimes = listOf(LocalTime.of(8, 0), LocalTime.of(20, 0)),
                instructions = "Sciacquare i semi sotto acqua corrente 2 volte al giorno. " +
                    "Mantenere coperti con un tessuto traspirante.",
            )
        )

        val trasferimentoPhaseId = phaseDao.insert(
            TemplatePhaseEntity(templateId = templateId, orderIndex = 2, name = "Trasferimento nel vassoio", durationDays = 1)
        )
        stepDao.insert(
            TemplateStepEntity(
                phaseId = trasferimentoPhaseId,
                orderIndex = 0,
                name = "Trasferimento nel vassoio",
                actionType = ActionType.TRAY_TRANSFER,
                offsetStartDays = 0,
                offsetEndDays = 0,
                reminderTimes = listOf(LocalTime.of(8, 0)),
                instructions = "Mettere i semi nel vassoio assicurandosi che non si sovrappongano. " +
                    "Mantenerli coperti dalla luce nelle prime fasi di crescita.",
            )
        )

        val crescitaPhaseId = phaseDao.insert(
            TemplatePhaseEntity(templateId = templateId, orderIndex = 3, name = "Crescita e raccolto", durationDays = null)
        )
        stepDao.insertAll(
            listOf(
                TemplateStepEntity(
                    phaseId = crescitaPhaseId,
                    orderIndex = 0,
                    name = "Crescita",
                    actionType = ActionType.LIGHT_GROWTH,
                    offsetStartDays = 0,
                    offsetEndDays = 3,
                    reminderTimes = listOf(LocalTime.of(8, 0)),
                    instructions = "Esporre le piante alla luce indiretta per almeno 6 ore. Annaffiare da sotto.",
                ),
                TemplateStepEntity(
                    phaseId = crescitaPhaseId,
                    orderIndex = 1,
                    name = "Raccolta",
                    actionType = ActionType.HARVEST,
                    offsetStartDays = 4,
                    offsetEndDays = 5,
                    reminderTimes = listOf(LocalTime.of(8, 0)),
                    instructions = "Ciclo totale 8-9 giorni dalla semina: raccogliere quando appaiono " +
                        "le prime vere foglie. Tagliare il più vicino alla radice possibile.",
                ),
                TemplateStepEntity(
                    phaseId = crescitaPhaseId,
                    orderIndex = 2,
                    name = "Conservazione",
                    actionType = ActionType.STORAGE,
                    offsetStartDays = 4,
                    offsetEndDays = null,
                    reminderTimes = emptyList(),
                    instructions = "Mantenuti in frigo in una busta sigillata durano una decina di giorni.",
                ),
            )
        )
    }
}
