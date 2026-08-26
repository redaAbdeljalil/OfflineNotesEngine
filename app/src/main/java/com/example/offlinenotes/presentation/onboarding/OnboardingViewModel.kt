package com.example.offlinenotes.presentation.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.offlinenotes.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val repository: SettingsRepository
) : ViewModel() {

    fun completeOnboarding() = viewModelScope.launch {
        repository.setOnboardingCompleted(true)
    }
}
