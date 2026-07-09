package com.dedo94.microgreensapp.core.repository

import com.dedo94.microgreensapp.core.database.dao.WeatherDailyDao
import com.dedo94.microgreensapp.core.database.entity.WeatherDailyEntity
import com.dedo94.microgreensapp.core.network.dto.ForecastResponseDto
import com.dedo94.microgreensapp.core.network.dto.GeocodingResponseDto
import com.dedo94.microgreensapp.core.network.dto.GeocodingResultDto
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
import java.time.LocalDateTime
import java.time.LocalTime
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
                    .addQueryParameter("daily", "sunrise,sunset")
                    .addQueryParameter("timezone", "auto")
                    .build()
                val body = execute(url) ?: return@runCatching null
                val response = json.decodeFromString<ForecastResponseDto>(body)
                WeatherDailyEntity(
                    date = today,
                    fetchedTemperature = response.current?.temperature,
                    fetchedHumidity = response.current?.relativeHumidity,
                    fetchedSunrise = response.daily?.sunrise?.firstOrNull()?.let(::parseIsoLocalTime),
                    fetchedSunset = response.daily?.sunset?.firstOrNull()?.let(::parseIsoLocalTime),
                    fetchedAt = Instant.now(),
                    locationNameSnapshot = location.name,
                ).also { weatherDailyDao.insert(it) }
            }.getOrNull()
        }
    }

    private fun execute(url: HttpUrl): String? {
        val request = Request.Builder().url(url).build()
        return httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            response.body?.string()
        }
    }

    private fun parseIsoLocalTime(value: String): LocalTime? =
        runCatching { LocalDateTime.parse(value).toLocalTime() }.getOrNull()
}
