package com.dedo94.microgreensapp.core.network

import com.dedo94.microgreensapp.core.network.dto.GeocodingResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenMeteoGeocodingApi {
    @GET("v1/search")
    suspend fun search(
        @Query("name") name: String,
        @Query("count") count: Int = 10,
        @Query("language") language: String = "it",
        @Query("format") format: String = "json",
    ): GeocodingResponseDto
}
