package com.example.offlinenotes.presentation.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.offlinenotes.domain.model.Note
import com.example.offlinenotes.domain.usecase.NoteUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrashViewModel @Inject constructor(
    private val useCases: NoteUseCases
) : ViewModel() {

    val deletedNotes: StateFlow<List<Note>> = useCases.getDeletedNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun restoreNote(note: Note) = viewModelScope.launch {
        useCases.saveNote(note.copy(isDeleted = false, updatedAt = System.currentTimeMillis()))
    }

    fun emptyTrash() = viewModelScope.launch {
        useCases.emptyTrash()
    }
}
