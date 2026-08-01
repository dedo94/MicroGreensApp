package com.dedo94.microgreensapp.feature.tray

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dedo94.microgreensapp.core.database.entity.SubstrateType
import com.dedo94.microgreensapp.core.database.entity.TrayEntity
import com.dedo94.microgreensapp.core.repository.TrayRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Niente più SavedStateHandle/route: il redesign v2 apre questo form come
 * bottom-sheet locale in TrayDetailScreen invece che come schermata di
 * navigazione, quindi trayId arriva come parametro esplicito via [load]
 * (chiamato una volta da un LaunchedEffect) invece che da un argomento di
 * rotta.
 */
@HiltViewModel
class TrayEditViewModel @Inject constructor(
    private val repository: TrayRepository,
) : ViewModel() {

    private var existingTray: TrayEntity? = null

    var trayName by mutableStateOf("")
        private set
    var seedQuantityText by mutableStateOf("")
        private set
    var substrateType by mutableStateOf(SubstrateType.HYDROPONIC_MAT)
        private set
    var substrateNotes by mutableStateOf("")
        private set
    var isLoaded by mutableStateOf(false)
        private set

    val canSave: Boolean
        get() = trayName.isNotBlank()

    fun load(trayId: Long) {
        viewModelScope.launch {
            repository.observeTray(trayId).firstOrNull()?.let { tray ->
                existingTray = tray
                trayName = tray.name
                seedQuantityText = tray.initialSeedQuantityGrams?.toString() ?: ""
                substrateType = tray.substrateType
                substrateNotes = tray.substrateNotes
                isLoaded = true
            }
        }
    }

    fun onNameChange(value: String) {
        trayName = value
    }

    fun onSeedQuantityChange(value: String) {
        seedQuantityText = value.filter { it.isDigit() || it == '.' }
    }

    fun onSubstrateTypeChange(type: SubstrateType) {
        substrateType = type
    }

    fun onSubstrateNotesChange(value: String) {
        substrateNotes = value
    }

    fun save(onSaved: () -> Unit) {
        val tray = existingTray ?: return
        if (trayName.isBlank()) return
        viewModelScope.launch {
            repository.updateTrayDetails(
                tray = tray,
                name = trayName,
                initialSeedQuantityGrams = seedQuantityText.toDoubleOrNull(),
                substrateType = substrateType,
                substrateNotes = substrateNotes,
            )
            onSaved()
        }
    }
}
