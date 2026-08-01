package com.dedo94.microgreensapp.core.repository

import android.content.Context
import android.content.res.Configuration
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.darkModePreferenceDataStore by preferencesDataStore(name = "dark_mode_preferences")

/**
 * Preferenza manuale del tema chiaro/scuro, salvata in DataStore (non
 * Room). Finché l'utente non tocca lo switch "Tema scuro" in Opzioni, il
 * valore di default rispecchia il tema di sistema corrente (letto da
 * [Configuration] anziché da isSystemInDarkTheme(), non disponibile fuori
 * da un contesto Compose) — un cambio di tema di sistema prima del primo
 * tocco resta quindi trasparente, dato che Android ricrea l'Activity sul
 * cambio uiMode e questo default viene ricalcolato da zero.
 */
@Singleton
class DarkModePreferenceRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val DARK_ENABLED = booleanPreferencesKey("dark_mode_enabled")
    }

    private val systemDefault: Boolean
        get() {
            val uiMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            return uiMode == Configuration.UI_MODE_NIGHT_YES
        }

    val darkModeEnabled: Flow<Boolean> = context.darkModePreferenceDataStore.data.map { prefs ->
        prefs[Keys.DARK_ENABLED] ?: systemDefault
    }

    suspend fun setDarkModeEnabled(enabled: Boolean) {
        context.darkModePreferenceDataStore.edit { prefs -> prefs[Keys.DARK_ENABLED] = enabled }
    }
}
