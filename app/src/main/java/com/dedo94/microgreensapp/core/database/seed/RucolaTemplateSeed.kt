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
 * Template precaricato per la rucola. A differenza di girasole/piselli/melone
 * NON prevede ammollo: il seme di rucola è mucillaginoso (forma un gel a
 * contatto con l'acqua) e va seminato a secco direttamente sul substrato
 * umido. Dati dalle guide di coltivazione microgreens: germinazione 2-3
 * giorni, buio ~3 giorni, raccolta a 7-10 giorni dalla semina — qui fissata
 * a un giorno unico (il 9, punto medio della finestra) su richiesta
 * dell'utente, invece di un'occorrenza per ogni giorno della finestra.
 * Regolabile da "Gestisci varietà" se l'esperienza reale differisce.
 */
object RucolaTemplateSeed {

    private const val PLANT_TYPE = "Eruca vesicaria"

    suspend fun seedIfNeeded(
        templateDao: VarietyTemplateDao,
        phaseDao: TemplatePhaseDao,
        stepDao: TemplateStepDao,
    ) {
        if (templateDao.getByName("Rucola") != null) return

        val templateId = templateDao.insert(
            VarietyTemplateEntity(
                name = "Rucola",
                plantType = PLANT_TYPE,
                notes = "Semina a secco, niente ammollo: il seme è mucillaginoso.",
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
                instructions = "NON ammollare: il seme forma un gel con l'acqua. Distribuire i semi " +
                    "asciutti in modo uniforme sul substrato già umido, nebulizzare in superficie " +
                    "e coprire il vassoio.",
            )
        )

        val buioPhaseId = phaseDao.insert(
            TemplatePhaseEntity(templateId = templateId, orderIndex = 1, name = "Buio e germinazione", durationDays = 3)
        )
        stepDao.insert(
            TemplateStepEntity(
                phaseId = buioPhaseId,
                orderIndex = 0,
                name = "Nebulizzazione",
                actionType = ActionType.WATERING,
                offsetStartDays = 0,
                offsetEndDays = 2,
                reminderTimes = listOf(LocalTime.of(8, 0), LocalTime.of(20, 0)),
                instructions = "Tenere il vassoio coperto e al buio. Nebulizzare 2 volte al giorno: " +
                    "substrato umido ma non fradicio. La germinazione parte in 2-3 giorni.",
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
                    offsetEndDays = 4,
                    reminderTimes = listOf(LocalTime.of(8, 0)),
                    instructions = "Scoprire ed esporre alla luce indiretta per almeno 6 ore. " +
                        "Annaffiare da sotto per non bagnare le foglioline.",
                ),
                TemplateStepEntity(
                    phaseId = crescitaPhaseId,
                    orderIndex = 1,
                    name = "Raccolta",
                    actionType = ActionType.HARVEST,
                    offsetStartDays = 5,
                    offsetEndDays = 5,
                    reminderTimes = listOf(LocalTime.of(8, 0)),
                    instructions = "Con piantine di ~5cm, tagliare con le forbici appena sopra il " +
                        "substrato. Sapore piccante-nocciolato. La finestra reale è 7-10 giorni " +
                        "dalla semina: si può anticipare o posticipare di un giorno o due.",
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
