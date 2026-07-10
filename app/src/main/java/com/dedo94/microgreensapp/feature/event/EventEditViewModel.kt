package com.dedo94.microgreensapp.feature.event

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.dedo94.microgreensapp.core.database.entity.ActionType
import com.dedo94.microgreensapp.core.database.entity.EventEntity
import com.dedo94.microgreensapp.core.repository.TrayRepository
import com.dedo94.microgreensapp.core.repository.WeatherRepository
import com.dedo94.microgreensapp.navigation.EventEditRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class EventEditViewModel @Inject constructor(
    private val repository: TrayRepository,
    private val weatherRepository: WeatherRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val route: EventEditRoute = savedStateHandle.toRoute()
    val trayId: Long = route.trayId
    val isNew: Boolean = route.eventId == 0L

    private var existingEvent: EventEntity? = null

    var eventType by mutableStateOf(ActionType.NOTE)
        private set
    var eventDate by mutableStateOf(LocalDate.now())
        private set
    var eventTimeText by mutableStateOf("")
        private set
    var title by mutableStateOf("")
        private set
    var notes by mutableStateOf("")
        private set
    var quantityText by mutableStateOf("")
        private set
    var quantityUnit by mutableStateOf("")
        private set
    var temperatureText by mutableStateOf("")
        private set
    var humidityText by mutableStateOf("")
        private set
    var lightNotes by mutableStateOf("")
        private set

    val canSave: Boolean
        get() = title.isNotBlank()

    init {
        if (isNew) {
            prefillFromWeather()
        } else {
            viewModelScope.launch {
                repository.getEvent(route.eventId)?.let { event ->
                    existingEvent = event
                    eventType = event.eventType
                    eventDate = event.eventDate
                    eventTimeText = event.eventTime?.toString() ?: ""
                    title = event.title
                    notes = event.notes
                    quantityText = event.quantityValue?.toString() ?: ""
                    quantityUnit = event.quantityUnit
                    temperatureText = event.actualTemperature?.toString() ?: ""
                    humidityText = event.actualHumidity?.toString() ?: ""
                    lightNotes = event.actualLightNotes
                }
            }
        }
    }

    private fun prefillFromWeather() {
        viewModelScope.launch {
            val weather = weatherRepository.fetchTodayIfNeeded() ?: return@launch
            if (temperatureText.isBlank()) {
                temperatureText = weather.fetchedTemperature?.toString() ?: ""
            }
            if (humidityText.isBlank()) {
                humidityText = weather.fetchedHumidity?.toString() ?: ""
            }
        }
    }

    fun onTypeChange(type: ActionType) {
        eventType = type
    }

    fun onDateChange(date: LocalDate) {
        eventDate = date
    }

    fun onTimeTextChange(value: String) {
        eventTimeText = value
    }

    fun onTitleChange(value: String) {
        title = value
    }

    fun onNotesChange(value: String) {
        notes = value
    }

    fun onQuantityChange(value: String) {
        quantityText = value.filter { it.isDigit() || it == '.' }
    }

    fun onQuantityUnitChange(value: String) {
        quantityUnit = value
    }

    fun onTemperatureChange(value: String) {
        temperatureText = value.filter { it.isDigit() || it == '.' || it == '-' }
    }

    fun onHumidityChange(value: String) {
        humidityText = value.filter { it.isDigit() || it == '.' }
    }

    fun onLightNotesChange(value: String) {
        lightNotes = value
    }

    fun save(onSaved: () -> Unit) {
        if (title.isBlank()) return
        val time = eventTimeText.takeIf { it.isNotBlank() }
            ?.let { runCatching { LocalTime.parse(it) }.getOrNull() }
        val quantity = quantityText.toDoubleOrNull()
        viewModelScope.launch {
            val base = existingEvent ?: EventEntity(
                trayId = trayId,
                trayStepId = null,
                eventDate = eventDate,
                eventType = eventType,
                title = title,
            )
            val event = base.copy(
                eventType = eventType,
                eventDate = eventDate,
                eventTime = time,
                title = title,
                notes = notes,
                quantityValue = quantity,
                quantityUnit = quantityUnit,
                actualTemperature = temperatureText.toDoubleOrNull(),
                actualHumidity = humidityText.toDoubleOrNull(),
                actualLightNotes = lightNotes,
            )
            if (existingEvent == null) {
                repository.addEvent(event)
            } else {
                repository.updateEvent(event)
            }
            onSaved()
        }
    }
}
