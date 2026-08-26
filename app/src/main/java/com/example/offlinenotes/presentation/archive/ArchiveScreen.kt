package com.example.offlinenotes.presentation.archive

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.offlinenotes.presentation.components.EmptyState
import com.example.offlinenotes.presentation.components.NoteItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveScreen(
    onNavigateBack: () -> Unit,
    onNoteClick: (String) -> Unit,
    viewModel: ArchiveViewModel = hiltViewModel()
) {
    val notes by viewModel.archivedNotes.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Archive") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, "Back") }
                }
            )
        }
    ) { padding ->
        if (notes.isEmpty()) {
            EmptyState(
                icon = Icons.Outlined.Archive,
                title = "Archive is empty",
                description = "Notes you archive will appear here.",
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalItemSpacing = 12.dp,
                modifier = Modifier.padding(padding).fillMaxSize()
            ) {
                items(notes) { note ->
                    NoteItem(note = note, onClick = { onNoteClick(note.id) })
                }
            }
        }
    }
}
