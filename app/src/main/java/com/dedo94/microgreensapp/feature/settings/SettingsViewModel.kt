package com.dedo94.microgreensapp.feature.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dedo94.microgreensapp.core.network.dto.GeocodingResultDto
import com.dedo94.microgreensapp.core.notifications.NotificationScheduler
import com.dedo94.microgreensapp.core.repository.LocationPreference
import com.dedo94.microgreensapp.core.repository.NotificationPreferenceRepository
import com.dedo94.microgreensapp.core.repository.TrayRepository
import com.dedo94.microgreensapp.core.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val notificationPreferenceRepository: NotificationPreferenceRepository,
    private val trayRepository: TrayRepository,
    private val notificationScheduler: NotificationScheduler,
) : ViewModel() {

    val location: StateFlow<LocationPreference?> = weatherRepository.observeLocation()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val notificationsEnabled: StateFlow<Boolean> = notificationPreferenceRepository.enabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    fun onNotificationsEnabledChange(enabled: Boolean) {
        viewModelScope.launch {
            notificationPreferenceRepository.setEnabled(enabled)
            if (enabled) {
                trayRepository.rescheduleAllReminders()
            } else {
                notificationScheduler.cancelAllReminders()
            }
        }
    }

    var query by mutableStateOf("")
        private set
    var results by mutableStateOf<List<GeocodingResultDto>>(emptyList())
        private set
    var isSearching by mutableStateOf(false)
        private set

    fun onQueryChange(value: String) {
        query = value
    }

    fun search() {
        if (query.isBlank()) return
        viewModelScope.launch {
            isSearching = true
            results = weatherRepository.searchLocations(query)
            isSearching = false
        }
    }

    fun selectLocation(result: GeocodingResultDto) {
        viewModelScope.launch {
            weatherRepository.setLocation(result)
            results = emptyList()
            query = ""
        }
    }
}
