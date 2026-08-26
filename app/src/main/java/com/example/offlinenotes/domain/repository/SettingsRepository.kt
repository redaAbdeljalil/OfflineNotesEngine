package com.example.offlinenotes.domain.repository

import com.example.offlinenotes.domain.model.AppTheme
import com.example.offlinenotes.domain.model.EditorFont
import com.example.offlinenotes.domain.model.NoteSorting
import com.example.offlinenotes.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<AppSettings>
    suspend fun setTheme(theme: AppTheme)
    suspend fun setSorting(sorting: NoteSorting)
    suspend fun setFont(font: EditorFont)
    suspend fun setSyncEnabled(enabled: Boolean)
    suspend fun setDefaultColor(colorHex: String?)
    suspend fun setOnboardingCompleted(completed: Boolean)
}
