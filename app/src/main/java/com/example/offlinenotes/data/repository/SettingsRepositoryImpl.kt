package com.example.offlinenotes.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.offlinenotes.domain.model.AppTheme
import com.example.offlinenotes.domain.model.AppSettings
import com.example.offlinenotes.domain.model.EditorFont
import com.example.offlinenotes.domain.model.NoteSorting
import com.example.offlinenotes.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepositoryImpl @Inject constructor(
    private val context: Context
) : SettingsRepository {

    private object Keys {
        val THEME = stringPreferencesKey("theme")
        val SORTING = stringPreferencesKey("sorting")
        val FONT = stringPreferencesKey("font")
        val SYNC_ENABLED = booleanPreferencesKey("sync_enabled")
        val DEFAULT_COLOR = stringPreferencesKey("default_color")
    }

    override val settings: Flow<AppSettings> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            AppSettings(
                theme = AppTheme.valueOf(preferences[Keys.THEME] ?: AppTheme.SYSTEM.name),
                defaultSorting = NoteSorting.valueOf(preferences[Keys.SORTING] ?: NoteSorting.UPDATED.name),
                editorFont = EditorFont.valueOf(preferences[Keys.FONT] ?: EditorFont.SANS.name),
                syncEnabled = preferences[Keys.SYNC_ENABLED] ?: true,
                defaultColorHex = preferences[Keys.DEFAULT_COLOR]
            )
        }

    override suspend fun setTheme(theme: AppTheme) {
        context.dataStore.edit { it[Keys.THEME] = theme.name }
    }

    override suspend fun setSorting(sorting: NoteSorting) {
        context.dataStore.edit { it[Keys.SORTING] = sorting.name }
    }

    override suspend fun setFont(font: EditorFont) {
        context.dataStore.edit { it[Keys.FONT] = font.name }
    }

    override suspend fun setSyncEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SYNC_ENABLED] = enabled }
    }

    override suspend fun setDefaultColor(colorHex: String?) {
        context.dataStore.edit { 
            if (colorHex == null) it.remove(Keys.DEFAULT_COLOR)
            else it[Keys.DEFAULT_COLOR] = colorHex
        }
    }
}
