package com.dedo94.microgreensapp.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate

/**
 * Cache del meteo Open-Meteo per un giorno: una riga per data, assumendo una
 * sola posizione attiva alla volta. Rappresenta solo "cosa ha detto l'API";
 * i valori effettivamente usati per le statistiche vivono su [EventEntity]
 * (actualTemperature/actualHumidity), pre-compilati da questa cache ma
 * sempre sovrascrivibili dall'utente.
 */
@Entity(tableName = "weather_daily")
data class WeatherDailyEntity(
    @PrimaryKey
    val date: LocalDate,
    val fetchedTemperature: Double?,
    val fetchedHumidity: Double?,
    val fetchedAt: Instant,
    val locationNameSnapshot: String,
)
