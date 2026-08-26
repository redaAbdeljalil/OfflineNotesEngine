package com.example.offlinenotes.domain.model

enum class AppTheme {
    LIGHT, DARK, SYSTEM
}

enum class NoteSorting {
    UPDATED, CREATED, ALPHABETICAL, PINNED
}

enum class EditorFont {
    SANS, SERIF, MONOSPACE
}

data class AppSettings(
    val theme: AppTheme = AppTheme.SYSTEM,
    val defaultSorting: NoteSorting = NoteSorting.UPDATED,
    val editorFont: EditorFont = EditorFont.SANS,
    val syncEnabled: Boolean = true,
    val defaultColorHex: String? = null
)
