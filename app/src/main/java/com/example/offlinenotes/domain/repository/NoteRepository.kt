package com.example.offlinenotes.domain.repository

import com.example.offlinenotes.domain.model.Note
import com.example.offlinenotes.domain.model.NoteSorting
import com.example.offlinenotes.domain.model.NoteVersion
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getActiveNotes(query: String = "", sorting: NoteSorting = NoteSorting.UPDATED): Flow<List<Note>>
    fun getArchivedNotes(): Flow<List<Note>>
    fun getDeletedNotes(): Flow<List<Note>>
    fun getNoteById(id: String): Flow<Note?>
    fun getNoteVersions(noteId: String): Flow<List<NoteVersion>>

    suspend fun saveNote(note: Note)
    suspend fun deleteNote(id: String)
    suspend fun deleteNotePermanently(id: String)
    suspend fun restoreVersion(version: NoteVersion)
    suspend fun emptyTrash()
    suspend fun triggerSync()
}