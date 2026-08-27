package com.example.offlinenotes.data.mapper

import com.example.offlinenotes.data.local.entity.NoteEntity
import com.example.offlinenotes.data.local.entity.NoteVersionEntity
import com.example.offlinenotes.domain.model.Note
import com.example.offlinenotes.domain.model.NoteVersion

fun NoteEntity.toDomain(): Note = Note(
    id = id, 
    title = title, 
    content = content, 
    createdAt = createdAt,
    updatedAt = updatedAt, 
    isPinned = isPinned, 
    isArchived = isArchived,
    isDeleted = isDeleted, 
    version = version, 
    syncStatus = syncStatus,
    colorHex = colorHex, 
    tags = tags
)

fun Note.toEntity(): NoteEntity = NoteEntity(
    id = id, 
    title = title, 
    content = content, 
    createdAt = createdAt,
    updatedAt = updatedAt, 
    isPinned = isPinned, 
    isArchived = isArchived,
    isDeleted = isDeleted, 
    version = version, 
    syncStatus = syncStatus,
    colorHex = colorHex, 
    tags = tags
)

fun NoteVersionEntity.toDomain(): NoteVersion = NoteVersion(
    id = id, 
    noteId = noteId, 
    title = title, 
    content = content,
    savedAt = savedAt, 
    versionNumber = versionNumber
)

fun NoteVersion.toEntity(): NoteVersionEntity = NoteVersionEntity(
    id = id, 
    noteId = noteId, 
    title = title, 
    content = content,
    savedAt = savedAt, 
    versionNumber = versionNumber
)