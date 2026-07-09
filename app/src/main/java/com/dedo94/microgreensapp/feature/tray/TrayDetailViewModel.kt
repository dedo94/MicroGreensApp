package com.dedo94.microgreensapp.feature.tray

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.dedo94.microgreensapp.core.database.entity.EventEntity
import com.dedo94.microgreensapp.core.database.entity.PhotoEntity
import com.dedo94.microgreensapp.core.database.entity.TrayEntity
import com.dedo94.microgreensapp.core.database.entity.TrayStatus
import com.dedo94.microgreensapp.core.database.entity.TrayStepEntity
import com.dedo94.microgreensapp.core.repository.PhotoRepository
import com.dedo94.microgreensapp.core.repository.TrayRepository
import com.dedo94.microgreensapp.navigation.TrayDetailRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class TrayDetailViewModel @Inject constructor(
    private val repository: TrayRepository,
    private val photoRepository: PhotoRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val route: TrayDetailRoute = savedStateHandle.toRoute()
    val trayId: Long = route.trayId

    val tray: StateFlow<TrayEntity?> = repository.observeTray(trayId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val steps: StateFlow<List<TrayStepEntity>> = repository.stepsForTray(trayId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val events: StateFlow<List<EventEntity>> = repository.eventsForTray(trayId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val photos: StateFlow<List<PhotoEntity>> = photoRepository.observePhotosForTray(trayId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun photoFile(photo: PhotoEntity): File = photoRepository.fileFor(photo)

    fun createCaptureTarget(): Pair<File, Uri> = photoRepository.createCaptureTarget()

    fun onPhotoCaptured(file: File) {
        viewModelScope.launch { photoRepository.savePhoto(file, trayId, eventId = null) }
    }

    fun onPhotoPicked(uri: Uri) {
        viewModelScope.launch { photoRepository.importFromUri(uri, trayId, eventId = null) }
    }

    fun deletePhoto(photo: PhotoEntity) {
        viewModelScope.launch { photoRepository.deletePhoto(photo) }
    }

    fun markDone(step: TrayStepEntity) {
        viewModelScope.launch { repository.markStepDone(step) }
    }

    fun markSkipped(step: TrayStepEntity) {
        viewModelScope.launch { repository.markStepSkipped(step) }
    }

    fun updateStep(step: TrayStepEntity) {
        viewModelScope.launch { repository.updateTrayStep(step) }
    }

    fun deleteStep(step: TrayStepEntity) {
        viewModelScope.launch { repository.deleteTrayStep(step) }
    }

    fun deleteEvent(event: EventEntity) {
        viewModelScope.launch { repository.deleteEvent(event) }
    }

    fun setStatus(status: TrayStatus) {
        val current = tray.value ?: return
        viewModelScope.launch { repository.updateTrayStatus(current, status) }
    }

    fun deleteTray(onDeleted: () -> Unit) {
        val current = tray.value ?: return
        viewModelScope.launch {
            repository.deleteTray(current)
            onDeleted()
        }
    }
}
