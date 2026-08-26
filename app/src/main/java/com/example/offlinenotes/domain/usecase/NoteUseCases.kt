package com.example.offlinenotes.domain.usecase

import com.example.offlinenotes.domain.model.Note
import com.example.offlinenotes.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NoteUseCases @Inject constructor(
    private val repository: NoteRepository
) {
    fun getNotes(query: String): Flow<List<Note>> = repository.getActiveNotes(query)
    fun getNoteDetails(id: String): Flow<Note?> = repository.getNoteById(id)
    fun getNoteHistory(id: String) = repository.getNoteVersions(id)

    suspend fun saveNote(note: Note) = repository.saveNote(note)
    suspend fun pinNote(note: Note) = repository.saveNote(note.copy(isPinned = !note.isPinned))
    suspend fun archiveNote(note: Note) = repository.saveNote(note.copy(isArchived = !note.isArchived))
    suspend fun deleteNote(id: String) = repository.deleteNote(id)

    suspend fun restoreVersion(noteId: String, versionId: String) {
        // Find version and restore (handled via repo to track versions)
        repository.getNoteVersions(noteId).collect { versions ->
            versions.find { it.id == versionId }?.let {
                repository.restoreVersion(it)
            }
        }
    }
}