package com.dedo94.microgreensapp.core.repository

import com.dedo94.microgreensapp.core.database.dao.WeatherDailyDao
import com.dedo94.microgreensapp.core.database.entity.WeatherDailyEntity
import com.dedo94.microgreensapp.core.network.OpenMeteoForecastApi
import com.dedo94.microgreensapp.core.network.OpenMeteoGeocodingApi
import com.dedo94.microgreensapp.core.network.dto.GeocodingResultDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor(
    private val geocodingApi: OpenMeteoGeocodingApi,
    private val forecastApi: OpenMeteoForecastApi,
    private val weatherDailyDao: WeatherDailyDao,
    private val locationRepository: LocationRepository,
) {
    fun observeLocation(): Flow<LocationPreference?> = locationRepository.location

    suspend fun searchLocations(query: String): List<GeocodingResultDto> {
        if (query.isBlank()) return emptyList()
        return runCatching { geocodingApi.search(name = query).results.orEmpty() }
            .getOrElse { emptyList() }
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
        return runCatching {
            val response = forecastApi.getForecast(
                latitude = location.latitude,
                longitude = location.longitude,
            )
            val entity = WeatherDailyEntity(
                date = today,
                fetchedTemperature = response.current?.temperature,
                fetchedHumidity = response.current?.relativeHumidity,
                fetchedSunrise = response.daily?.sunrise?.firstOrNull()?.let(::parseIsoLocalTime),
                fetchedSunset = response.daily?.sunset?.firstOrNull()?.let(::parseIsoLocalTime),
                fetchedAt = Instant.now(),
                locationNameSnapshot = location.name,
            )
            weatherDailyDao.insert(entity)
            entity
        }.getOrNull()
    }

    private fun parseIsoLocalTime(value: String): LocalTime? =
        runCatching { LocalDateTime.parse(value).toLocalTime() }.getOrNull()
}
