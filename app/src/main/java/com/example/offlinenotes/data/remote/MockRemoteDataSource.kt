package com.example.offlinenotes.data.remote

import com.example.offlinenotes.domain.model.Note
import com.example.offlinenotes.domain.model.SyncConflictException
import kotlinx.coroutines.delay
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockRemoteDataSource @Inject constructor() {
    private val serverDb = ConcurrentHashMap<String, Note>()

    suspend fun pushNote(localNote: Note): Note {
        delay(600) // Simulate latency
        val serverNote = serverDb[localNote.id]

        if (serverNote != null && serverNote.version > localNote.version) {
            // Conflict: Server has a newer version.
            throw SyncConflictException(serverNote)
        }

        // Success: Bump version for server
        val updatedNote = localNote.copy(version = localNote.version + 1)
        serverDb[updatedNote.id] = updatedNote
        return updatedNote
    }

    suspend fun deleteNote(id: String) {
        delay(400)
        serverDb.remove(id)
    }
}