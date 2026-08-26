package com.example.offlinenotes.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.offlinenotes.domain.model.NoteVersion
import com.example.offlinenotes.domain.usecase.NoteUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val useCases: NoteUseCases
) : ViewModel() {

    private val _versions = MutableStateFlow<List<NoteVersion>>(emptyList())
    val versions = _versions.asStateFlow()

    fun loadHistory(noteId: String) {
        viewModelScope.launch {
            useCases.getNoteHistory(noteId).collect {
                _versions.value = it
            }
        }
    }

    fun restore(noteId: String, versionId: String) {
        viewModelScope.launch {
            useCases.restoreVersion(noteId, versionId)
        }
    }
}