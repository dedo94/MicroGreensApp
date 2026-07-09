package com.dedo94.microgreensapp.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

/**
 * Un evento registrato nel diario del vassoio: può soddisfare uno step
 * pianificato (trayStepId non nullo) oppure essere una nota/azione libera
 * (trayStepId nullo), permettendo di deviare dal piano senza modificarlo.
 */
@Entity(
    tableName = "events",
    foreignKeys = [
        ForeignKey(
            entity = TrayEntity::class,
            parentColumns = ["id"],
            childColumns = ["trayId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = TrayStepEntity::class,
            parentColumns = ["id"],
            childColumns = ["trayStepId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("trayId"), Index("trayStepId"), Index("eventDate")],
)
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val trayId: Long,
    val trayStepId: Long?,
    val eventDate: LocalDate,
    val eventTime: LocalTime? = null,
    val eventType: ActionType,
    val title: String,
    val notes: String = "",
    val quantityValue: Double? = null,
    val quantityUnit: String = "",
    val actualTemperature: Double? = null,
    val actualHumidity: Double? = null,
    val actualLightNotes: String = "",
    val createdAt: Instant = Instant.now(),
)
