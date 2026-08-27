package com.example.offlinenotes

import com.example.offlinenotes.data.local.OfflineNotesDatabase
import com.example.offlinenotes.data.local.dao.NoteDao
import com.example.offlinenotes.data.local.dao.SyncDao
import com.example.offlinenotes.data.local.dao.VersionDao
import com.example.offlinenotes.data.local.entity.NoteEntity
import com.example.offlinenotes.data.local.entity.SyncOperationEntity
import com.example.offlinenotes.data.remote.MockRemoteDataSource
import com.example.offlinenotes.data.sync.SyncEngine
import com.example.offlinenotes.domain.model.Note
import com.example.offlinenotes.domain.model.SyncConflictException
import com.example.offlinenotes.domain.model.SyncOperationType
import com.example.offlinenotes.domain.model.SyncStatus
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

class SyncEngineTest {

    private lateinit var syncEngine: SyncEngine
    private val database = mock(OfflineNotesDatabase::class.java)
    private val remoteData = mock(MockRemoteDataSource::class.java)
    private val noteDao = mock(NoteDao::class.java)
    private val syncDao = mock(SyncDao::class.java)
    private val versionDao = mock(VersionDao::class.java)

    @Before
    fun setup() {
        `when`(database.noteDao).thenReturn(noteDao)
        `when`(database.syncDao).thenReturn(syncDao)
        `when`(database.versionDao).thenReturn(versionDao)
        syncEngine = SyncEngine(database, remoteData)
    }

    @Test
    fun `test conflict resolution saves history and overwrites local`() = runTest {
        val noteId = "note1"
        val op = SyncOperationEntity("op1", noteId, SyncOperationType.UPDATE, 0L)
        val localNote = NoteEntity(noteId, "Local", "A", 0L, 0L, false, false, false, 1, SyncStatus.PENDING)
        val remoteNoteDomain = Note(noteId, "Remote", "B", 0L, 0L, false, false, false, 2, SyncStatus.SYNCED)

        `when`(syncDao.getPendingOperations()).thenReturn(listOf(op))
        `when`(noteDao.getNoteByIdSync(noteId)).thenReturn(localNote)
        `when`(remoteData.pushNote(any())).thenThrow(SyncConflictException(remoteNoteDomain))

        val success = syncEngine.performSync()

        assertTrue(success)
        verify(versionDao).insertVersion(argThat { it.content == "A" }) 
        verify(noteDao).insertNote(argThat { it.content == "B" && it.syncStatus == SyncStatus.SYNCED }) 
        verify(syncDao).deleteOperation(op)
    }
}