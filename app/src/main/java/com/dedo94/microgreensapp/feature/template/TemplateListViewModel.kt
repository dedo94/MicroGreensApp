package com.dedo94.microgreensapp.feature.template

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dedo94.microgreensapp.core.database.entity.VarietyTemplateEntity
import com.dedo94.microgreensapp.core.repository.TemplateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TemplateListViewModel @Inject constructor(
    private val repository: TemplateRepository,
) : ViewModel() {

    val templates: StateFlow<List<VarietyTemplateEntity>> = repository.activeTemplates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun deleteTemplate(template: VarietyTemplateEntity) {
        viewModelScope.launch {
            repository.deleteTemplate(template)
        }
    }
}
