package com.dedo94.microgreensapp.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate

/**
 * Un vassoio fisico in coltivazione (o completato). Riferisce il template da
 * cui è nato solo per etichetta/statistiche: alla creazione il piano viene
 * copiato in [TrayStepEntity], quindi modificare il template non influenza
 * mai un vassoio già creato.
 */
@Entity(
    tableName = "trays",
    foreignKeys = [
        ForeignKey(
            entity = VarietyTemplateEntity::class,
            parentColumns = ["id"],
            childColumns = ["varietyTemplateId"],
            onDelete = ForeignKey.SET_NULL,
        )
    ],
    indices = [Index("varietyTemplateId")],
)
data class TrayEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val varietyTemplateId: Long?,
    val varietyName: String,
    val sowingDate: LocalDate,
    val initialSeedQuantityGrams: Double?,
    val substrateType: SubstrateType,
    val substrateNotes: String = "",
    val colorTag: Int? = null,
    val status: TrayStatus = TrayStatus.IN_PROGRESS,
    val createdAt: Instant = Instant.now(),
)
