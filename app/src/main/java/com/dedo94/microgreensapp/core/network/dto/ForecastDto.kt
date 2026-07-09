package com.dedo94.microgreensapp.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ForecastResponseDto(
    val current: CurrentWeatherDto? = null,
    val daily: DailyWeatherDto? = null,
)

@Serializable
data class CurrentWeatherDto(
    @SerialName("temperature_2m") val temperature: Double? = null,
    @SerialName("relative_humidity_2m") val relativeHumidity: Double? = null,
)

@Serializable
data class DailyWeatherDto(
    val sunrise: List<String>? = null,
    val sunset: List<String>? = null,
)
