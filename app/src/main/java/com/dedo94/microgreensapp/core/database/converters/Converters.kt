package com.dedo94.microgreensapp.core.database.converters

import androidx.room.TypeConverter
import com.dedo94.microgreensapp.core.database.entity.ActionType
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

/**
 * Converter Room condivisi. Gli enum sono serializzati per nome (mai
 * ordinale) così un riordino dei valori nel codice non corrompe i dati
 * salvati.
 */
class Converters {

    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let(LocalDate::parse)

    @TypeConverter
    fun fromLocalTime(value: LocalTime?): String? = value?.toString()

    @TypeConverter
    fun toLocalTime(value: String?): LocalTime? = value?.let(LocalTime::parse)

    @TypeConverter
    fun fromInstant(value: Instant?): Long? = value?.toEpochMilli()

    @TypeConverter
    fun toInstant(value: Long?): Instant? = value?.let(Instant::ofEpochMilli)

    @TypeConverter
    fun fromActionType(value: ActionType): String = value.name

    @TypeConverter
    fun toActionType(value: String): ActionType = ActionType.valueOf(value)

    @TypeConverter
    fun fromLocalTimeList(value: List<LocalTime>): String =
        Json.encodeToString(value.map { it.toString() })

    @TypeConverter
    fun toLocalTimeList(value: String): List<LocalTime> =
        if (value.isBlank()) {
            emptyList()
        } else {
            Json.decodeFromString<List<String>>(value).map(LocalTime::parse)
        }
}
