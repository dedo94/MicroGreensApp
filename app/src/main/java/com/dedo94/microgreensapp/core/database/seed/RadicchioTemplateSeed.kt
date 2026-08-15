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
 * Template precaricato per il radicchio (famiglia della cicoria). Come la
 * rucola NON prevede ammollo (seme piccolo, si semina a secco) e — dalle
 * guide sulla cicoria da microgreens — non richiede nemmeno un vero periodo
 * di buio. Germinazione ~3 giorni tenendo umido, raccolta a 10-14 giorni
 * dalla semina, sapore leggermente amaro. Regolabile da "Gestisci varietà"
 * se l'esperienza reale differisce.
 */
object RadicchioTemplateSeed {

    private const val PLANT_TYPE = "Cichorium intybus"

    suspend fun seedIfNeeded(
        templateDao: VarietyTemplateDao,
        phaseDao: TemplatePhaseDao,
        stepDao: TemplateStepDao,
    ) {
        if (templateDao.getByName("Radicchio") != null) return

        val templateId = templateDao.insert(
            VarietyTemplateEntity(
                name = "Radicchio",
                plantType = PLANT_TYPE,
                notes = "Semina a secco, niente ammollo e niente fase di buio obbligatoria.",
            )
        )

        val seminaPhaseId = phaseDao.insert(
            TemplatePhaseEntity(templateId = templateId, orderIndex = 0, name = "Semina", durationDays = 1)
        )
        stepDao.insert(
            TemplateStepEntity(
                phaseId = seminaPhaseId,
                orderIndex = 0,
                name = "Semina nel vassoio",
                actionType = ActionType.TRAY_TRANSFER,
                offsetStartDays = 0,
                offsetEndDays = 0,
                reminderTimes = listOf(LocalTime.of(8, 0)),
                instructions = "Distribuire i semi asciutti in modo uniforme sul substrato già " +
                    "umido e nebulizzare in superficie. Niente ammollo: il seme è piccolo.",
            )
        )

        val germinazionePhaseId = phaseDao.insert(
            TemplatePhaseEntity(templateId = templateId, orderIndex = 1, name = "Germinazione", durationDays = 3)
        )
        stepDao.insert(
            TemplateStepEntity(
                phaseId = germinazionePhaseId,
                orderIndex = 0,
                name = "Nebulizzazione",
                actionType = ActionType.WATERING,
                offsetStartDays = 0,
                offsetEndDays = 2,
                reminderTimes = listOf(LocalTime.of(8, 0), LocalTime.of(20, 0)),
                instructions = "Nebulizzare 1-2 volte al giorno: substrato umido ma non fradicio, " +
                    "temperatura ideale 18-24°C. Non serve tenere al buio.",
            )
        )

        val crescitaPhaseId = phaseDao.insert(
            TemplatePhaseEntity(templateId = templateId, orderIndex = 2, name = "Crescita e raccolto", durationDays = null)
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
                    instructions = "Esporre alla luce indiretta per almeno 6 ore. Annaffiare da " +
                        "sotto per non bagnare le foglioline.",
                ),
                TemplateStepEntity(
                    phaseId = crescitaPhaseId,
                    orderIndex = 1,
                    name = "Raccolta",
                    actionType = ActionType.HARVEST,
                    offsetStartDays = 6,
                    offsetEndDays = 10,
                    reminderTimes = listOf(LocalTime.of(8, 0)),
                    instructions = "Raccolta a 10-14 giorni dalla semina. Tagliare con le forbici " +
                        "appena sopra il substrato. Sapore leggermente amaro, si attenua maturando.",
                ),
                TemplateStepEntity(
                    phaseId = crescitaPhaseId,
                    orderIndex = 2,
                    name = "Conservazione",
                    actionType = ActionType.STORAGE,
                    offsetStartDays = 6,
                    offsetEndDays = null,
                    reminderTimes = emptyList(),
                    instructions = "Mantenuti in frigo in una busta sigillata durano una decina di giorni.",
                ),
            )
        )
    }
}
