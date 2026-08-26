package com.example.offlinenotes.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.offlinenotes.domain.model.AppTheme
import com.example.offlinenotes.domain.model.AppSettings
import com.example.offlinenotes.domain.model.EditorFont
import com.example.offlinenotes.domain.model.NoteSorting
import com.example.offlinenotes.domain.repository.SettingsRepository
import com.example.offlinenotes.domain.usecase.NoteUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository,
    private val noteUseCases: NoteUseCases
) : ViewModel() {

    val settings: StateFlow<AppSettings> = repository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    fun setTheme(theme: AppTheme) = viewModelScope.launch {
        repository.setTheme(theme)
    }

    fun setSorting(sorting: NoteSorting) = viewModelScope.launch {
        repository.setSorting(sorting)
    }

    fun setFont(font: EditorFont) = viewModelScope.launch {
        repository.setFont(font)
    }

    fun setSyncEnabled(enabled: Boolean) = viewModelScope.launch {
        repository.setSyncEnabled(enabled)
        if (enabled) {
            noteUseCases.triggerSync()
        }
    }

    fun setDefaultColor(colorHex: String?) = viewModelScope.launch {
        repository.setDefaultColor(colorHex)
    }
}
