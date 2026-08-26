package com.example.offlinenotes.presentation.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.offlinenotes.domain.model.Note
import com.example.offlinenotes.domain.model.SyncStatus
import com.example.offlinenotes.domain.usecase.NoteUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class EditorState(
    val title: String = "",
    val content: String = "",
    val noteId: String = UUID.randomUUID().toString(),
    val isExisting: Boolean = false,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
    val colorHex: String? = null,
    val tags: List<String> = emptyList(),
    val isSaving: Boolean = false
)

@HiltViewModel
class EditorViewModel @Inject constructor(
    private val useCases: NoteUseCases
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditorState())
    val uiState = _uiState.asStateFlow()

    private val undoStack = mutableListOf<EditorState>()
    private val redoStack = mutableListOf<EditorState>()
    private var saveJob: Job? = null

    fun loadNote(noteId: String) {
        if (noteId == "new") {
            _uiState.update { it.copy(noteId = UUID.randomUUID().toString(), isExisting = false) }
            return
        }
        viewModelScope.launch {
            useCases.getNoteDetails(noteId).collect { note ->
                note?.let {
                    val state = EditorState(
                        it.title, it.content, it.id, true, it.syncStatus,
                        it.colorHex, it.tags
                    )
                    _uiState.value = state
                    if (undoStack.isEmpty()) undoStack.add(state)
                }
            }
        }
    }

    fun onTitleChange(newTitle: String) {
        pushToUndoStack()
        _uiState.update { it.copy(title = newTitle) }
        scheduleSave()
    }

    fun onContentChange(newContent: String) {
        pushToUndoStack()
        _uiState.update { it.copy(content = newContent) }
        scheduleSave()
    }

    fun onColorChange(colorHex: String?) {
        pushToUndoStack()
        _uiState.update { it.copy(colorHex = colorHex) }
        saveImmediately()
    }

    fun onTagsChange(tags: List<String>) {
        pushToUndoStack()
        _uiState.update { it.copy(tags = tags) }
        saveImmediately()
    }

    private fun pushToUndoStack() {
        undoStack.add(_uiState.value.copy(isSaving = false))
        if (undoStack.size > 50) undoStack.removeAt(0)
        redoStack.clear()
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            redoStack.add(_uiState.value.copy(isSaving = false))
            _uiState.value = undoStack.removeLast()
            saveImmediately()
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            undoStack.add(_uiState.value.copy(isSaving = false))
            _uiState.value = redoStack.removeLast()
            saveImmediately()
        }
    }

    private fun scheduleSave() {
        saveJob?.cancel()
        _uiState.update { it.copy(isSaving = true) }
        saveJob = viewModelScope.launch {
            delay(1000)
            saveImmediately()
        }
    }

    fun saveImmediately() {
        val state = _uiState.value
        if (state.title.isBlank() && state.content.isBlank()) return

        viewModelScope.launch {
            useCases.saveNote(
                Note(
                    id = state.noteId,
                    title = state.title,
                    content = state.content,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    isPinned = false,
                    isArchived = false,
                    isDeleted = false,
                    version = 0,
                    syncStatus = SyncStatus.PENDING,
                    colorHex = state.colorHex,
                    tags = state.tags
                )
            )
            _uiState.update { it.copy(isSaving = false) }
        }
    }
}