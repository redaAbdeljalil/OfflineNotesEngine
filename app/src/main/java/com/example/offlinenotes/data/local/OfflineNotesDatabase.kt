package com.example.offlinenotes.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.offlinenotes.data.local.dao.NoteDao
import com.example.offlinenotes.data.local.dao.SyncDao
import com.example.offlinenotes.data.local.dao.VersionDao
import com.example.offlinenotes.data.local.entity.NoteEntity
import com.example.offlinenotes.data.local.entity.NoteVersionEntity
import com.example.offlinenotes.data.local.entity.SyncOperationEntity

@Database(
    entities = [NoteEntity::class, NoteVersionEntity::class, SyncOperationEntity::class],
    version = 1,
    exportSchema = true
)
abstract class OfflineNotesDatabase : RoomDatabase() {
    abstract val noteDao: NoteDao
    abstract val versionDao: VersionDao
    abstract val syncDao: SyncDao
}