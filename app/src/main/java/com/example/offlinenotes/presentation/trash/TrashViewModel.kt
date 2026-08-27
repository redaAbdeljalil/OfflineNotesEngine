package com.example.offlinenotes.presentation.trash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.offlinenotes.domain.model.Note
import com.example.offlinenotes.domain.usecase.NoteUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TrashUiState(
    val notes: List<Note> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class TrashViewModel @Inject constructor(
    private val useCases: NoteUseCases
) : ViewModel() {

    val uiState: StateFlow<TrashUiState> = useCases.getDeletedNotes()
        .map { notes -> TrashUiState(notes = notes) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TrashUiState(isLoading = true)
        )

    fun restoreNote(note: Note) = viewModelScope.launch {
        useCases.saveNote(note.copy(isDeleted = false, updatedAt = System.currentTimeMillis()))
    }

    fun emptyTrash() = viewModelScope.launch {
        useCases.emptyTrash()
    }

    fun deleteNotePermanently(id: String) = viewModelScope.launch {
        useCases.deleteNotePermanently(id)
    }
}
