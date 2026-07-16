package com.dedo94.microgreensapp.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

/**
 * Snapshot di una singola occorrenza di uno step per un vassoio specifico:
 * un giorno preciso, e un orario preciso se lo step ne prevede uno (null se
 * lo step non ha promemoria, es. Conservazione). Uno step con più orari al
 * giorno (es. sciacquo mattina/sera) produce più righe, una per occorrenza,
 * completabili indipendentemente l'una dall'altra.
 *
 * Copiato una volta da [TemplateStepEntity] alla creazione del vassoio: da
 * quel momento è completamente indipendente dal template (può essere
 * modificato, saltato, o il vassoio può avere step aggiuntivi con
 * isAdHoc = true) senza mai più leggere il template. [phaseName] e
 * [phaseOrderIndex] sono denormalizzati dalla fase di provenienza allo
 * stesso modo: un vassoio non ha una propria tabella di fasi, il
 * raggruppamento per fase si ottiene raggruppando le righe per phaseName.
 */
@Entity(
    tableName = "tray_steps",
    foreignKeys = [
        ForeignKey(
            entity = TrayEntity::class,
            parentColumns = ["id"],
            childColumns = ["trayId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("trayId")],
)
data class TrayStepEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val trayId: Long,
    val sourceTemplateStepId: Long?,
    val orderIndex: Int,
    val name: String,
    val actionType: ActionType,
    val plannedDate: LocalDate,
    val plannedTime: LocalTime?,
    val durationHours: Int? = null,
    val phaseName: String,
    val phaseOrderIndex: Int,
    val instructions: String = "",
    val status: TrayStepStatus = TrayStepStatus.PENDING,
    val isAdHoc: Boolean = false,
)
