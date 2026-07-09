package com.dedo94.microgreensapp.core.repository

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

data class LocationPreference(
    val name: String,
    val latitude: Double,
    val longitude: Double,
)

/**
 * Posizione usata per il meteo, salvata in DataStore (non Room): è un
 * singolo valore di impostazione globale, cambiabile in qualsiasi momento
 * dalle Impostazioni, non un'entità relazionale.
 */
@Singleton
class LocationRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val NAME = stringPreferencesKey("location_name")
        val LATITUDE = doublePreferencesKey("location_latitude")
        val LONGITUDE = doublePreferencesKey("location_longitude")
    }

    val location: Flow<LocationPreference?> = context.settingsDataStore.data.map { prefs ->
        val name = prefs[Keys.NAME]
        val latitude = prefs[Keys.LATITUDE]
        val longitude = prefs[Keys.LONGITUDE]
        if (name != null && latitude != null && longitude != null) {
            LocationPreference(name, latitude, longitude)
        } else {
            null
        }
    }

    suspend fun setLocation(name: String, latitude: Double, longitude: Double) {
        context.settingsDataStore.edit { prefs ->
            prefs[Keys.NAME] = name
            prefs[Keys.LATITUDE] = latitude
            prefs[Keys.LONGITUDE] = longitude
        }
    }
}
