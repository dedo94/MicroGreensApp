package com.dedo94.microgreensapp.core.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.notificationPreferenceDataStore by preferencesDataStore(name = "notification_preferences")

/**
 * Interruttore generale dei promemoria, salvato in DataStore (non Room):
 * un valore di impostazione globale, indipendente dal permesso di sistema
 * POST_NOTIFICATIONS. Attivo di default per non cambiare il comportamento
 * di chi ha già l'app installata.
 */
@Singleton
class NotificationPreferenceRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val ENABLED = booleanPreferencesKey("notifications_enabled")
    }

    val enabled: Flow<Boolean> = context.notificationPreferenceDataStore.data.map { prefs ->
        prefs[Keys.ENABLED] ?: true
    }

    suspend fun setEnabled(enabled: Boolean) {
        context.notificationPreferenceDataStore.edit { prefs -> prefs[Keys.ENABLED] = enabled }
    }
}
