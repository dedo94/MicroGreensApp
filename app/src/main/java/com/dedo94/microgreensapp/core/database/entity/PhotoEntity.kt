package com.dedo94.microgreensapp.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Una foto allegata a un vassoio, opzionalmente collegata a un evento
 * specifico. trayId è denormalizzato anche quando eventId è presente, per
 * poter mostrare "tutte le foto del vassoio" con una sola query.
 */
@Entity(
    tableName = "photos",
    foreignKeys = [
        ForeignKey(
            entity = TrayEntity::class,
            parentColumns = ["id"],
            childColumns = ["trayId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = EventEntity::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("trayId"), Index("eventId")],
)
data class PhotoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val trayId: Long,
    val eventId: Long? = null,
    val filePath: String,
    val caption: String = "",
    val createdAt: Instant = Instant.now(),
)
