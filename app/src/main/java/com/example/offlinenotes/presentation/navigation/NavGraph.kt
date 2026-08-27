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
    startDestination: String = Screen.Home.route
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
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(
                onNoteClick = { noteId -> navController.navigate(Screen.Editor.createRoute(noteId)) },
                onCreateNote = { navController.navigate(Screen.Editor.createRoute("new")) },
                onSettingsClick = { navController.navigate(Screen.Settings.route) },
                onArchiveClick = { navController.navigate(Screen.Archive.route) }
            )
        }
        composable(Screen.Editor.route) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId") ?: "new"
            EditorScreen(
                noteId = noteId,
                onNavigateBack = { navController.popBackStack() },
                onViewHistory = { id -> navController.navigate(Screen.History.createRoute(id)) }
            )
        }
        composable(Screen.History.route) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId") ?: return@composable
            HistoryScreen(
                noteId = noteId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Archive.route) {
            ArchiveScreen(
                onNavigateBack = { navController.popBackStack() },
                onNoteClick = { noteId -> navController.navigate(Screen.Editor.createRoute(noteId)) }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onTrashClick = { navController.navigate(Screen.Trash.route) }
            )
        }
        composable(Screen.Trash.route) {
            TrashScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}