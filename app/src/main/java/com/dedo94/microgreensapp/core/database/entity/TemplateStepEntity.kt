package com.dedo94.microgreensapp.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalTime

/**
 * Uno step della "ricetta" di una fase: definisce quando e cosa fare,
 * espresso come offset in giorni dall'inizio della fase che lo contiene
 * (non dalla semina, e non date assolute: il template non è legato a
 * nessuna semina specifica). Il numero di occorrenze giornaliere è
 * implicito nella dimensione di [reminderTimes] (vuota = una volta al
 * giorno senza orario specifico).
 */
@Entity(
    tableName = "template_steps",
    foreignKeys = [
        ForeignKey(
            entity = TemplatePhaseEntity::class,
            parentColumns = ["id"],
            childColumns = ["phaseId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("phaseId")],
)
data class TemplateStepEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val phaseId: Long,
    val orderIndex: Int,
    val name: String,
    val actionType: ActionType,
    val offsetStartDays: Int,
    val offsetEndDays: Int? = null,
    val durationHours: Int? = null,
    val reminderTimes: List<LocalTime> = emptyList(),
    val instructions: String = "",
)
