package com.dedo94.microgreensapp.core.network

import com.dedo94.microgreensapp.core.network.dto.ForecastResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenMeteoForecastApi {
    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "temperature_2m,relative_humidity_2m",
        @Query("daily") daily: String = "sunrise,sunset",
        @Query("timezone") timezone: String = "auto",
    ): ForecastResponseDto
}
