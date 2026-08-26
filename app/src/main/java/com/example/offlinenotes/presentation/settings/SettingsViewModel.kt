package com.example.offlinenotes.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.offlinenotes.domain.model.AppTheme
import com.example.offlinenotes.domain.model.AppSettings
import com.example.offlinenotes.domain.model.EditorFont
import com.example.offlinenotes.domain.model.NoteSorting
import com.example.offlinenotes.domain.repository.SecurityRepository
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
    private val securityRepository: SecurityRepository,
    private val noteUseCases: NoteUseCases
) : ViewModel() {

    val settings: StateFlow<AppSettings> = repository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    val isBiometricEnabled: StateFlow<Boolean> = securityRepository.isBiometricEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isScreenshotProtected: StateFlow<Boolean> = securityRepository.isScreenshotProtectionEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

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

    fun completeOnboarding(completed: Boolean) = viewModelScope.launch {
        repository.setOnboardingCompleted(completed)
    }

    fun setBiometricEnabled(enabled: Boolean) = viewModelScope.launch {
        securityRepository.setBiometricEnabled(enabled)
    }

    fun setScreenshotProtection(enabled: Boolean) = viewModelScope.launch {
        securityRepository.setScreenshotProtectionEnabled(enabled)
    }
}
