package com.dedo94.microgreensapp.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

/**
 * Snapshot di uno step per un vassoio specifico, con date assolute calcolate
 * dalla data di semina. Copiato una volta da [TemplateStepEntity] alla
 * creazione del vassoio: da quel momento è completamente indipendente dal
 * template (può essere modificato, saltato, o il vassoio può avere step
 * aggiuntivi con isAdHoc = true) senza mai più leggere il template.
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
    val plannedStartDate: LocalDate,
    val plannedEndDate: LocalDate,
    val durationHours: Int? = null,
    val repeatPerDay: Int = 1,
    val reminderTimes: List<LocalTime> = emptyList(),
    val instructions: String = "",
    val status: TrayStepStatus = TrayStepStatus.PENDING,
    val isAdHoc: Boolean = false,
)
