package com.dedo94.microgreensapp.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalTime

/**
 * Uno step della "ricetta" di una varietà: definisce quando e cosa fare,
 * espresso come offset in giorni dalla data di semina (non date assolute,
 * perché il template non è legato a nessuna semina specifica).
 */
@Entity(
    tableName = "template_steps",
    foreignKeys = [
        ForeignKey(
            entity = VarietyTemplateEntity::class,
            parentColumns = ["id"],
            childColumns = ["templateId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("templateId")],
)
data class TemplateStepEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val templateId: Long,
    val orderIndex: Int,
    val name: String,
    val actionType: ActionType,
    val offsetStartDays: Int,
    val offsetEndDays: Int? = null,
    val durationHours: Int? = null,
    val repeatPerDay: Int = 1,
    val reminderTimes: List<LocalTime> = emptyList(),
    val instructions: String = "",
)
