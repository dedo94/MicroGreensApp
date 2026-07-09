package com.dedo94.microgreensapp.feature.tray

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.dedo94.microgreensapp.core.database.entity.SubstrateType
import com.dedo94.microgreensapp.core.database.entity.TrayEntity
import com.dedo94.microgreensapp.core.repository.TrayRepository
import com.dedo94.microgreensapp.navigation.TrayEditRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrayEditViewModel @Inject constructor(
    private val repository: TrayRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val route: TrayEditRoute = savedStateHandle.toRoute()
    private var existingTray: TrayEntity? = null

    var trayName by mutableStateOf("")
        private set
    var seedQuantityText by mutableStateOf("")
        private set
    var substrateType by mutableStateOf(SubstrateType.SOIL)
        private set
    var substrateNotes by mutableStateOf("")
        private set
    var isLoaded by mutableStateOf(false)
        private set

    val canSave: Boolean
        get() = trayName.isNotBlank()

    init {
        viewModelScope.launch {
            repository.observeTray(route.trayId).firstOrNull()?.let { tray ->
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
