package com.dedo94.microgreensapp.feature.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dedo94.microgreensapp.core.repository.StatsOverview
import com.dedo94.microgreensapp.core.repository.StatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    repository: StatsRepository,
) : ViewModel() {

    val overview: StateFlow<StatsOverview?> = repository.observeOverview()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
