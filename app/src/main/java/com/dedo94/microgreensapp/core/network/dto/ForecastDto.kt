package com.dedo94.microgreensapp.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ForecastResponseDto(
    val current: CurrentWeatherDto? = null,
)

@Serializable
data class CurrentWeatherDto(
    @SerialName("temperature_2m") val temperature: Double? = null,
    @SerialName("relative_humidity_2m") val relativeHumidity: Double? = null,
)

@Serializable
data class HistoricalWeatherResponseDto(
    val daily: DailyHistoricalDto? = null,
)

@Serializable
data class DailyHistoricalDto(
    val time: List<String> = emptyList(),
    @SerialName("temperature_2m_mean") val temperatureMean: List<Double?> = emptyList(),
    @SerialName("relative_humidity_2m_mean") val relativeHumidityMean: List<Double?> = emptyList(),
)
