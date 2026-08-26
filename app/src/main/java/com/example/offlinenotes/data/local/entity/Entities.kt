package com.example.offlinenotes.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.offlinenotes.domain.model.SyncOperationType
import com.example.offlinenotes.domain.model.SyncStatus

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isPinned: Boolean,
    val isArchived: Boolean,
    val isDeleted: Boolean,
    val version: Int,
    val syncStatus: SyncStatus
)

@Entity(tableName = "note_versions")
data class NoteVersionEntity(
    @PrimaryKey val id: String,
    val noteId: String,
    val title: String,
    val content: String,
    val savedAt: Long,
    val versionNumber: Int
)

@Entity(tableName = "sync_queue")
data class SyncOperationEntity(
    @PrimaryKey val id: String,
    val noteId: String,
    val operationType: SyncOperationType,
    val createdAt: Long,
    val retryCount: Int = 0
)