package com.example.offlinenotes.presentation.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.offlinenotes.domain.model.Note
import com.example.offlinenotes.domain.model.NoteSorting
import com.example.offlinenotes.domain.repository.SettingsRepository
import com.example.offlinenotes.domain.usecase.NoteUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val useCases: NoteUseCases,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val notes: StateFlow<List<Note>> = combine(
        _searchQuery,
        settingsRepository.settings
    ) { query, settings ->
        query to settings.defaultSorting
    }.flatMapLatest { (query, sorting) ->
        useCases.getNotes(query, sorting)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun pinNote(note: Note) = viewModelScope.launch { useCases.pinNote(note) }
    fun deleteNote(note: Note) = viewModelScope.launch { useCases.deleteNote(note.id) }
}