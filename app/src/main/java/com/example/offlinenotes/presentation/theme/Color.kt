package com.example.offlinenotes.presentation.theme

import androidx.compose.ui.graphics.Color

// Primary Colors
val PrimaryLight = Color(0xFF1A1C1E)
val PrimaryDark = Color(0xFFE2E2E6)

// Surface Colors
val SurfaceLight = Color(0xFFF8FAFC)
val SurfaceVariantLight = Color(0xFFE2E8F0)
val SurfaceDark = Color(0xFF1E293B)
val SurfaceVariantDark = Color(0xFF334155)
val BackgroundDark = Color(0xFF0F172A)

// Accent Colors for Notes (Tasteful Pastels / Deep Tones)
val NoteDefault = Color.Transparent
val NoteWarm = Color(0xFFFEF3C7)
val NoteBlue = Color(0xFFDBEAFE)
val NoteGreen = Color(0xFFDCFCE7)
val NotePurple = Color(0xFFF3E8FF)
val NoteRose = Color(0xFFFFE4E6)

val NoteWarmDark = Color(0xFF451A03)
val NoteBlueDark = Color(0xFF1E3A8A)
val NoteGreenDark = Color(0xFF064E3B)
val NotePurpleDark = Color(0xFF581C87)
val NoteRoseDark = Color(0xFF881337)

fun getNoteColor(hex: String?, isDark: Boolean): Color {
    return when (hex) {
        "#FEF3C7" -> if (isDark) NoteWarmDark else NoteWarm
        "#DBEAFE" -> if (isDark) NoteBlueDark else NoteBlue
        "#DCFCE7" -> if (isDark) NoteGreenDark else NoteGreen
        "#F3E8FF" -> if (isDark) NotePurpleDark else NotePurple
        "#FFE4E6" -> if (isDark) NoteRoseDark else NoteRose
        else -> Color.Transparent
    }
}

// Functional Colors
val Error = Color(0xFFBA1A1A)
val Success = Color(0xFF2D6A4F)
val Info = Color(0xFF0061A4)
