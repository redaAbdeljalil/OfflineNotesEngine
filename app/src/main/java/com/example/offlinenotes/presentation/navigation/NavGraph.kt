package com.example.offlinenotes.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.offlinenotes.presentation.editor.EditorScreen
import com.example.offlinenotes.presentation.history.HistoryScreen
import com.example.offlinenotes.presentation.notes.HomeScreen

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onNoteClick = { noteId -> navController.navigate("editor/$noteId") },
                onCreateNote = { navController.navigate("editor/new") }
            )
        }
        composable("editor/{noteId}") { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId") ?: "new"
            EditorScreen(
                noteId = noteId,
                onNavigateBack = { navController.popBackStack() },
                onViewHistory = { id -> navController.navigate("history/$id") }
            )
        }
        composable("history/{noteId}") { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId") ?: return@composable
            HistoryScreen(
                noteId = noteId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}