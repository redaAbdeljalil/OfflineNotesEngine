package com.example.offlinenotes.domain.model

import androidx.compose.runtime.Immutable

enum class SyncStatus {
    SYNCED, PENDING, ERROR, CONFLICT
}

enum class SyncOperationType {
    CREATE, UPDATE, DELETE
}

@Immutable
data class Note(
    val id: String,
    val title: String,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long,
    val isPinned: Boolean,
    val isArchived: Boolean,
    val isDeleted: Boolean,
    val version: Int,
    val syncStatus: SyncStatus,
    val colorHex: String? = null,
    val tags: List<String> = emptyList()
)

@Immutable
data class NoteVersion(
    val id: String,
    val noteId: String,
    val title: String,
    val content: String,
    val savedAt: Long,
    val versionNumber: Int
)

class SyncConflictException(val serverNote: Note) : Exception("Conflict detected with remote version")