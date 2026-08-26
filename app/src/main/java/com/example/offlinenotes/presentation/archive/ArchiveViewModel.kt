package com.example.offlinenotes.presentation.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.offlinenotes.domain.model.Note
import com.example.offlinenotes.domain.usecase.NoteUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ArchiveViewModel @Inject constructor(
    private val useCases: NoteUseCases
) : ViewModel() {

    val archivedNotes: StateFlow<List<Note>> = useCases.getArchivedNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun unarchiveNote(note: Note) = viewModelScope.launch {
        useCases.archiveNote(note)
    }

    fun deleteNote(note: Note) = viewModelScope.launch {
        useCases.deleteNote(note.id)
    }
}
