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
 * Template precaricato al primo avvio, ricalcato esattamente sull'appunto
 * di coltivazione idroponica dei microgreens di girasole fornito dall'utente.
 *
 * Le fasi corrispondono esattamente alle date assolute del vecchio schema
 * piatto (offset dalla semina): "Crescita e raccolto" incapsula crescita,
 * raccolta e conservazione nella stessa fase perché la raccolta cade
 * sull'ultimo giorno di crescita, non il giorno dopo — tenerle separate
 * avrebbe spostato di un giorno la raccolta rispetto a prima.
 */
object SunflowerTemplateSeed {

    private const val PLANT_TYPE = "Helianthus Annuus"

    suspend fun seedIfNeeded(
        templateDao: VarietyTemplateDao,
        phaseDao: TemplatePhaseDao,
        stepDao: TemplateStepDao,
    ) {
        val existing = templateDao.getByName("Girasole")
        if (existing != null) {
            // Corregge il nome botanico su installazioni già seedate in
            // precedenza, senza toccare gli step già copiati sui vassoi.
            if (existing.plantType != PLANT_TYPE) {
                templateDao.update(existing.copy(plantType = PLANT_TYPE))
            }
            return
        }

        val templateId = templateDao.insert(
            VarietyTemplateEntity(
                name = "Girasole",
                plantType = PLANT_TYPE,
                notes = "Coltivazione idroponica dei microgreens di girasole.",
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
                durationHours = 8,
                reminderTimes = listOf(LocalTime.of(8, 0)),
                instructions = "8 ore di ammollo. I semi devono stare completamente sommersi.",
            )
        )

        val germinazionePhaseId = phaseDao.insert(
            TemplatePhaseEntity(templateId = templateId, orderIndex = 1, name = "Germinazione", durationDays = 3)
        )
        stepDao.insert(
            TemplateStepEntity(
                phaseId = germinazionePhaseId,
                orderIndex = 0,
                name = "Prevenzione muffa",
                actionType = ActionType.MOLD_PREVENTION,
                offsetStartDays = 0,
                offsetEndDays = 2,
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
                instructions = "Mettere i semi nel vassoio per la crescita. Mantenerli coperti " +
                    "dalla luce. Il peso sopra può essere facoltativo. Mantenere i semi idratati " +
                    "con uno spruzzino.",
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
                    offsetEndDays = 5,
                    reminderTimes = listOf(LocalTime.of(8, 0)),
                    instructions = "Se iniziano a crescere le radici sotto, esporre le piante alla " +
                        "luce indiretta per almeno 6 ore. Annaffiare da sotto.",
                ),
                TemplateStepEntity(
                    phaseId = crescitaPhaseId,
                    orderIndex = 1,
                    name = "Raccolta",
                    actionType = ActionType.HARVEST,
                    offsetStartDays = 5,
                    offsetEndDays = 5,
                    reminderTimes = listOf(LocalTime.of(8, 0)),
                    instructions = "Raccolta quando appaiono le prime vere foglie. Tagliare il più " +
                        "vicino alla radice possibile.",
                ),
                TemplateStepEntity(
                    phaseId = crescitaPhaseId,
                    orderIndex = 2,
                    name = "Conservazione",
                    actionType = ActionType.STORAGE,
                    offsetStartDays = 5,
                    offsetEndDays = null,
                    reminderTimes = emptyList(),
                    instructions = "Mantenuti in frigo in una busta sigillata durano una decina di giorni.",
                ),
            )
        )
    }
}
