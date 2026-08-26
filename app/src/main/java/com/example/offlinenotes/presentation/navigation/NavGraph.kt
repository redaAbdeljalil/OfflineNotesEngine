package com.example.offlinenotes.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.offlinenotes.presentation.archive.ArchiveScreen
import com.example.offlinenotes.presentation.editor.EditorScreen
import com.example.offlinenotes.presentation.history.HistoryScreen
import com.example.offlinenotes.presentation.notes.HomeScreen
import com.example.offlinenotes.presentation.onboarding.OnboardingScreen
import com.example.offlinenotes.presentation.settings.SettingsScreen
import com.example.offlinenotes.presentation.trash.TrashScreen

@Composable
fun AppNavGraph(
    startDestination: String = "home"
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(400)) + fadeIn(animationSpec = tween(400)) },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(400)) + fadeOut(animationSpec = tween(400)) },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(400)) + fadeIn(animationSpec = tween(400)) },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(400)) + fadeOut(animationSpec = tween(400)) }
    ) {
        composable("onboarding") {
            OnboardingScreen(
                onComplete = {
                    navController.navigate("home") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }
        composable("home") {
            HomeScreen(
                onNoteClick = { noteId -> navController.navigate("editor/$noteId") },
                onCreateNote = { navController.navigate("editor/new") },
                onSettingsClick = { navController.navigate("settings") },
                onArchiveClick = { navController.navigate("archive") }
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
        composable("archive") {
            ArchiveScreen(
                onNavigateBack = { navController.popBackStack() },
                onNoteClick = { noteId -> navController.navigate("editor/$noteId") }
            )
        }
        composable("settings") {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onTrashClick = { navController.navigate("trash") }
            )
        }
        composable("trash") {
            TrashScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}