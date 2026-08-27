package com.example.offlinenotes.data.repository

import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.offlinenotes.data.local.OfflineNotesDatabase
import com.example.offlinenotes.data.local.entity.SyncOperationEntity
import com.example.offlinenotes.data.mapper.toDomain
import com.example.offlinenotes.data.mapper.toEntity
import com.example.offlinenotes.data.sync.SyncWorker
import com.example.offlinenotes.domain.model.Note
import com.example.offlinenotes.domain.model.NoteSorting
import com.example.offlinenotes.domain.model.NoteVersion
import com.example.offlinenotes.domain.model.SyncOperationType
import com.example.offlinenotes.domain.model.SyncStatus
import com.example.offlinenotes.domain.repository.NoteRepository
import com.example.offlinenotes.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class NoteRepositoryImpl @Inject constructor(
    private val database: OfflineNotesDatabase,
    private val workManager: WorkManager,
    private val settingsRepository: SettingsRepository
) : NoteRepository {

    override fun getActiveNotes(query: String, sorting: NoteSorting): Flow<List<Note>> =
        database.noteDao.getActiveNotes(query, sorting.name).map { list -> list.map { it.toDomain() } }

    override fun getArchivedNotes(): Flow<List<Note>> =
        database.noteDao.getArchivedNotes().map { list -> list.map { it.toDomain() } }

    override fun getDeletedNotes(): Flow<List<Note>> =
        database.noteDao.getDeletedNotes().map { list -> list.map { it.toDomain() } }

    override fun getNoteById(id: String): Flow<Note?> =
        database.noteDao.getNoteById(id).map { it?.toDomain() }

    override fun getNoteVersions(noteId: String): Flow<List<NoteVersion>> =
        database.versionDao.getVersionsForNote(noteId).map { list -> list.map { it.toDomain() } }

    override suspend fun saveNote(note: Note) {
        val oldNote = database.noteDao.getNoteByIdSync(note.id)

        val pendingNote = note.copy(syncStatus = SyncStatus.PENDING, updatedAt = System.currentTimeMillis())
        database.noteDao.insertNote(pendingNote.toEntity())

        if (oldNote != null && (oldNote.content != note.content || oldNote.title != note.title)) {
            database.versionDao.insertVersion(
                NoteVersion(
                    id = UUID.randomUUID().toString(),
                    noteId = oldNote.id,
                    title = oldNote.title,
                    content = oldNote.content,
                    savedAt = System.currentTimeMillis(),
                    versionNumber = oldNote.version
                ).toEntity()
            )
        }

        queueSync(note.id, if (oldNote == null) SyncOperationType.CREATE else SyncOperationType.UPDATE)
    }

    override suspend fun deleteNote(id: String) {
        database.noteDao.markDeleted(id, SyncStatus.PENDING.name, System.currentTimeMillis())
        queueSync(id, SyncOperationType.DELETE)
    }

    override suspend fun restoreVersion(version: NoteVersion) {
        val current = database.noteDao.getNoteByIdSync(version.noteId) ?: return
        val restoredNote = current.copy(
            title = version.title,
            content = version.content,
            updatedAt = System.currentTimeMillis(),
            syncStatus = SyncStatus.PENDING
        )
        saveNote(restoredNote.toDomain()) 
    }

    override suspend fun emptyTrash() {
        database.noteDao.deleteTrash()
    }

    override suspend fun triggerSync() {
        triggerSyncWorker()
    }

    private suspend fun queueSync(noteId: String, type: SyncOperationType) {
        database.syncDao.insertOperation(
            SyncOperationEntity(
                id = UUID.randomUUID().toString(),
                noteId = noteId,
                operationType = type,
                createdAt = System.currentTimeMillis()
            )
        )
        if (settingsRepository.settings.first().syncEnabled) {
            triggerSyncWorker()
        }
    }

    private fun triggerSyncWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniqueWork(
            "SyncWorker",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
}