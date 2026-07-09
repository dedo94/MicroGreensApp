package com.dedo94.microgreensapp.core.di

import com.dedo94.microgreensapp.core.network.OpenMeteoForecastApi
import com.dedo94.microgreensapp.core.network.OpenMeteoGeocodingApi
import com.jakewharton.retrofit2.converter.kotlinx.serialization.KotlinSerializationConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
            .build()

    @Provides
    @Singleton
    fun provideGeocodingApi(client: OkHttpClient, json: Json): OpenMeteoGeocodingApi =
        Retrofit.Builder()
            .baseUrl("https://geocoding-api.open-meteo.com/")
            .client(client)
            .addConverterFactory(KotlinSerializationConverterFactory.create(json, "application/json".toMediaType()))
            .build()
            .create(OpenMeteoGeocodingApi::class.java)

    @Provides
    @Singleton
    fun provideForecastApi(client: OkHttpClient, json: Json): OpenMeteoForecastApi =
        Retrofit.Builder()
            .baseUrl("https://api.open-meteo.com/")
            .client(client)
            .addConverterFactory(KotlinSerializationConverterFactory.create(json, "application/json".toMediaType()))
            .build()
            .create(OpenMeteoForecastApi::class.java)
}
