package com.example.offlinenotes.presentation.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object Editor : Screen("editor/{noteId}") {
        fun createRoute(noteId: String) = "editor/$noteId"
    }
    object History : Screen("history/{noteId}") {
        fun createRoute(noteId: String) = "history/$noteId"
    }
    object Archive : Screen("archive")
    object Settings : Screen("settings")
    object Trash : Screen("trash")
}
