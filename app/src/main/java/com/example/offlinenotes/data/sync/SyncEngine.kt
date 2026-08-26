package com.example.offlinenotes.data.sync

import com.example.offlinenotes.data.local.OfflineNotesDatabase
import com.example.offlinenotes.data.mapper.toDomain
import com.example.offlinenotes.data.mapper.toEntity
import com.example.offlinenotes.data.remote.MockRemoteDataSource
import com.example.offlinenotes.domain.model.NoteVersion
import com.example.offlinenotes.domain.model.SyncConflictException
import com.example.offlinenotes.domain.model.SyncOperationType
import com.example.offlinenotes.domain.model.SyncStatus
import java.util.UUID
import javax.inject.Inject

class SyncEngine @Inject constructor(
    private val database: OfflineNotesDatabase,
    private val remoteData: MockRemoteDataSource
) {
    suspend fun performSync(): Boolean {
        val operations = database.syncDao.getPendingOperations()
        var allSuccess = true

        for (op in operations) {
            try {
                val localNoteEntity = database.noteDao.getNoteByIdSync(op.noteId) ?: continue

                if (op.operationType == SyncOperationType.DELETE) {
                    remoteData.deleteNote(op.noteId)
                } else {
                    val syncedNote = remoteData.pushNote(localNoteEntity.toDomain())
                    // Update local with server acknowledged version
                    database.noteDao.insertNote(syncedNote.copy(syncStatus = SyncStatus.SYNCED).toEntity())
                }
                database.syncDao.deleteOperation(op)

            } catch (e: SyncConflictException) {
                // CONFLICT RESOLUTION: Preserve local changes to history, adopt server state.
                val conflictingLocal = database.noteDao.getNoteByIdSync(op.noteId)!!

                database.versionDao.insertVersion(
                    NoteVersion(
                        id = UUID.randomUUID().toString(),
                        noteId = conflictingLocal.id,
                        title = conflictingLocal.title,
                        content = conflictingLocal.content,
                        savedAt = System.currentTimeMillis(),
                        versionNumber = conflictingLocal.version
                    ).toEntity()
                )
                // Overwrite local with remote
                database.noteDao.insertNote(e.serverNote.copy(syncStatus = SyncStatus.SYNCED).toEntity())
                database.syncDao.deleteOperation(op)

            } catch (e: Exception) {
                allSuccess = false
                database.syncDao.incrementRetry(op.id)
                // Mark note as ERROR state
                val note = database.noteDao.getNoteByIdSync(op.noteId)
                if (note != null) {
                    database.noteDao.insertNote(note.copy(syncStatus = SyncStatus.ERROR))
                }
            }
        }
        return allSuccess
    }
}