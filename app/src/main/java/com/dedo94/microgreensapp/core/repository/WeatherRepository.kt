package com.dedo94.microgreensapp.core.repository

import com.dedo94.microgreensapp.core.database.dao.WeatherDailyDao
import com.dedo94.microgreensapp.core.database.entity.WeatherDailyEntity
import com.dedo94.microgreensapp.core.network.dto.ForecastResponseDto
import com.dedo94.microgreensapp.core.network.dto.GeocodingResponseDto
import com.dedo94.microgreensapp.core.network.dto.GeocodingResultDto
import com.dedo94.microgreensapp.core.network.dto.HistoricalWeatherResponseDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Chiama direttamente gli endpoint gratuiti di Open-Meteo con OkHttp invece
 * che con Retrofit: per due sole GET con risposta JSON, evita di dipendere
 * da un converter Retrofit/kotlinx.serialization la cui API pubblica si è
 * rivelata diversa da quella documentata nelle versioni disponibili.
 */
@Singleton
class WeatherRepository @Inject constructor(
    private val httpClient: OkHttpClient,
    private val json: Json,
    private val weatherDailyDao: WeatherDailyDao,
    private val locationRepository: LocationRepository,
) {
    fun observeLocation(): Flow<LocationPreference?> = locationRepository.location

    suspend fun searchLocations(query: String): List<GeocodingResultDto> {
        if (query.isBlank()) return emptyList()
        return withContext(Dispatchers.IO) {
            runCatching {
                val url = "https://geocoding-api.open-meteo.com/v1/search".toHttpUrl().newBuilder()
                    .addQueryParameter("name", query)
                    .addQueryParameter("count", "10")
                    .addQueryParameter("language", "it")
                    .addQueryParameter("format", "json")
                    .build()
                val body = execute(url) ?: return@runCatching emptyList()
                json.decodeFromString<GeocodingResponseDto>(body).results.orEmpty()
            }.getOrElse { emptyList() }
        }
    }

    suspend fun setLocation(result: GeocodingResultDto) {
        val label = listOfNotNull(result.name, result.admin1, result.country)
            .distinct()
            .joinToString(", ")
        locationRepository.setLocation(label, result.latitude, result.longitude)
    }

    /**
     * Restituisce il meteo di oggi, usando la cache se già presente. Se manca
     * la posizione, o la chiamata di rete fallisce (es. offline), restituisce
     * null: il form evento resta semplicemente vuoto e compilabile a mano.
     */
    suspend fun fetchTodayIfNeeded(): WeatherDailyEntity? {
        val today = LocalDate.now()
        weatherDailyDao.getForDate(today)?.let { return it }

        val location = locationRepository.location.firstOrNull() ?: return null
        return withContext(Dispatchers.IO) {
            runCatching {
                val url = "https://api.open-meteo.com/v1/forecast".toHttpUrl().newBuilder()
                    .addQueryParameter("latitude", location.latitude.toString())
                    .addQueryParameter("longitude", location.longitude.toString())
                    .addQueryParameter("current", "temperature_2m,relative_humidity_2m")
                    .addQueryParameter("timezone", "auto")
                    .build()
                val body = execute(url) ?: return@runCatching null
                val response = json.decodeFromString<ForecastResponseDto>(body)
                WeatherDailyEntity(
                    date = today,
                    fetchedTemperature = response.current?.temperature,
                    fetchedHumidity = response.current?.relativeHumidity,
                    fetchedAt = Instant.now(),
                    locationNameSnapshot = location.name,
                ).also { weatherDailyDao.insert(it) }
            }.getOrNull()
        }
    }

    /**
     * Recupera il meteo storico per una lista di date passate (non oggi: per
     * quello c'è [fetchTodayIfNeeded]), in un'unica chiamata all'archivio
     * storico di Open-Meteo che copre l'intero intervallo min-max, salvando
     * ogni giorno trovato in cache. Usa la posizione attuale come
     * approssimazione anche per date in cui la posizione avrebbe potuto
     * essere diversa: l'app non tiene uno storico delle posizioni passate.
     *
     * Non verificabile da questo ambiente di sviluppo (rete verso
     * api.open-meteo.com bloccata): nomi dei parametri e dei campi
     * daily (`temperature_2m_mean`, `relative_humidity_2m_mean`) basati
     * sulla documentazione Open-Meteo, da confermare su dispositivo reale.
     */
    suspend fun fetchHistorical(dates: List<LocalDate>): Map<LocalDate, WeatherDailyEntity> {
        if (dates.isEmpty()) return emptyMap()
        val location = locationRepository.location.firstOrNull() ?: return emptyMap()
        val wantedDates = dates.toSet()
        val start = dates.min()
        val end = dates.max()
        return withContext(Dispatchers.IO) {
            runCatching {
                val url = "https://archive-api.open-meteo.com/v1/archive".toHttpUrl().newBuilder()
                    .addQueryParameter("latitude", location.latitude.toString())
                    .addQueryParameter("longitude", location.longitude.toString())
                    .addQueryParameter("start_date", start.toString())
                    .addQueryParameter("end_date", end.toString())
                    .addQueryParameter("daily", "temperature_2m_mean,relative_humidity_2m_mean")
                    .addQueryParameter("timezone", "auto")
                    .build()
                val body = execute(url) ?: return@runCatching emptyMap()
                val daily = json.decodeFromString<HistoricalWeatherResponseDto>(body).daily
                    ?: return@runCatching emptyMap()
                daily.time.indices.mapNotNull { i ->
                    val date = runCatching { LocalDate.parse(daily.time[i]) }.getOrNull()
                        ?.takeIf { it in wantedDates } ?: return@mapNotNull null
                    val entity = WeatherDailyEntity(
                        date = date,
                        fetchedTemperature = daily.temperatureMean.getOrNull(i),
                        fetchedHumidity = daily.relativeHumidityMean.getOrNull(i),
                        fetchedAt = Instant.now(),
                        locationNameSnapshot = location.name,
                    )
                    weatherDailyDao.insert(entity)
                    date to entity
                }.toMap()
            }.getOrElse { emptyMap() }
        }
    }

    private fun execute(url: HttpUrl): String? {
        val request = Request.Builder().url(url).build()
        return httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            response.body?.string()
        }
    }
}
