package com.example.offlinenotes.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.offlinenotes.domain.model.AppTheme
import com.example.offlinenotes.domain.repository.SecurityRepository
import com.example.offlinenotes.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainUiState(
    val theme: AppTheme = AppTheme.SYSTEM,
    val isBiometricEnabled: Boolean = false,
    val isScreenshotProtected: Boolean = false,
    val isOnboardingCompleted: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val securityRepository: SecurityRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState = _uiState.asStateFlow()

    private var _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated = _isAuthenticated.asStateFlow()

    init {
        combine(
            settingsRepository.settings,
            securityRepository.isBiometricEnabled,
            securityRepository.isScreenshotProtectionEnabled
        ) { settings, biometric, screenshot ->
            MainUiState(
                theme = settings.theme,
                isBiometricEnabled = biometric,
                isScreenshotProtected = screenshot,
                isOnboardingCompleted = settings.isOnboardingCompleted,
                isLoading = false
            )
        }.onEach { newState ->
            _uiState.value = newState
            // If biometric is disabled, auto-authenticate
            if (!newState.isBiometricEnabled) {
                _isAuthenticated.value = true
            }
        }.launchIn(viewModelScope)
    }

    fun setAuthenticated(authenticated: Boolean) {
        _isAuthenticated.value = authenticated
    }

    fun isDeviceRooted(): Boolean = securityRepository.isDeviceRooted()
}
