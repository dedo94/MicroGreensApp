package com.dedo94.microgreensapp.feature.tray

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dedo94.microgreensapp.core.database.entity.TrayEntity
import com.dedo94.microgreensapp.core.repository.TrayRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class TraysListViewModel @Inject constructor(
    repository: TrayRepository,
) : ViewModel() {

    val trays: StateFlow<List<TrayEntity>> = repository.observeTrays()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
