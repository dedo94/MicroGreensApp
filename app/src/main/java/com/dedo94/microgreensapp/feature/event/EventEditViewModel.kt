package com.dedo94.microgreensapp.feature.event

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dedo94.microgreensapp.core.database.entity.ActionType
import com.dedo94.microgreensapp.core.database.entity.EventEntity
import com.dedo94.microgreensapp.core.repository.TrayRepository
import com.dedo94.microgreensapp.core.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

/**
 * Niente più SavedStateHandle/route: il redesign v2 apre questo form come
 * bottom-sheet locale in TrayDetailScreen invece che come schermata di
 * navigazione, quindi trayId/eventId arrivano come parametri espliciti via
 * [load] (chiamato una volta da un LaunchedEffect) invece che da argomenti
 * di rotta. trayId/isNew restano observable (mutableStateOf) perché il
 * titolo del sheet ("Nuovo evento"/"Modifica evento") dipende da isNew.
 */
@HiltViewModel
class EventEditViewModel @Inject constructor(
    private val repository: TrayRepository,
    private val weatherRepository: WeatherRepository,
) : ViewModel() {

    var trayId by mutableStateOf(0L)
        private set
    var isNew by mutableStateOf(true)
        private set

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

    fun load(trayId: Long, eventId: Long) {
        this.trayId = trayId
        this.isNew = eventId == 0L
        if (isNew) {
            prefillFromWeather()
        } else {
            viewModelScope.launch {
                repository.getEvent(eventId)?.let { event ->
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
