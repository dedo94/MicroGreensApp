package com.dedo94.microgreensapp.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Una fase del ciclo di coltivazione di una varietà (es. Ammollo,
 * Germinazione, Crescita), contenitore ordinato di [TemplateStepEntity].
 * La durata è dichiarata esplicitamente (non derivata dagli step contenuti)
 * perché determina dove inizia la fase successiva: [durationDays] nullo è
 * ammesso solo sull'ultima fase di un template (fase aperta, es.
 * Conservazione), vincolo imposto in UI, non nello schema.
 */
@Entity(
    tableName = "template_phases",
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
data class TemplatePhaseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val templateId: Long,
    val orderIndex: Int,
    val name: String,
    val durationDays: Int? = null,
)
