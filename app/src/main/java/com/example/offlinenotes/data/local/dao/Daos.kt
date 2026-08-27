package com.example.offlinenotes.data.local.dao

import androidx.room.*
import com.example.offlinenotes.data.local.entity.NoteEntity
import com.example.offlinenotes.data.local.entity.NoteVersionEntity
import com.example.offlinenotes.data.local.entity.SyncOperationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE isDeleted = 0 AND isArchived = 0 AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%') ORDER BY " +
            "CASE WHEN :sort = 'PINNED' THEN isPinned END DESC, " +
            "CASE WHEN :sort = 'UPDATED' THEN updatedAt END DESC, " +
            "CASE WHEN :sort = 'CREATED' THEN createdAt END DESC, " +
            "CASE WHEN :sort = 'ALPHABETICAL' THEN title END ASC, " +
            "updatedAt DESC")
    fun getActiveNotes(query: String, sort: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE isArchived = 1 AND isDeleted = 0 ORDER BY updatedAt DESC")
    fun getArchivedNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE isDeleted = 1 ORDER BY updatedAt DESC")
    fun getDeletedNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id")
    fun getNoteById(id: String): Flow<NoteEntity?>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteByIdSync(id: String): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Query("UPDATE notes SET isDeleted = 1, syncStatus = :syncStatus, updatedAt = :timestamp WHERE id = :id")
    suspend fun markDeleted(id: String, syncStatus: String, timestamp: Long)

    @Query("DELETE FROM notes WHERE isDeleted = 1")
    suspend fun deleteTrash()

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: String)
}

@Dao
interface VersionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVersion(version: NoteVersionEntity)

    @Query("SELECT * FROM note_versions WHERE noteId = :noteId ORDER BY versionNumber DESC")
    fun getVersionsForNote(noteId: String): Flow<List<NoteVersionEntity>>
}

@Dao
interface SyncDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOperation(operation: SyncOperationEntity)

    @Query("SELECT * FROM sync_queue ORDER BY createdAt ASC")
    suspend fun getPendingOperations(): List<SyncOperationEntity>

    @Delete
    suspend fun deleteOperation(operation: SyncOperationEntity)

    @Query("UPDATE sync_queue SET retryCount = retryCount + 1 WHERE id = :id")
    suspend fun incrementRetry(id: String)
}