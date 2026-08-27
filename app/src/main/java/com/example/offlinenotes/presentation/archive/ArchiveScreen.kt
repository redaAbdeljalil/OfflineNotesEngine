package com.example.offlinenotes.presentation.archive

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Unarchive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.offlinenotes.R
import com.example.offlinenotes.domain.model.Note
import com.example.offlinenotes.presentation.components.EmptyState
import com.example.offlinenotes.presentation.components.NoteItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveScreen(
    onNavigateBack: () -> Unit,
    onNoteClick: (String) -> Unit,
    viewModel: ArchiveViewModel = hiltViewModel()
) {
    val notes by viewModel.archivedNotes.collectAsStateWithLifecycle()
    var selectedNoteForMenu by remember { mutableStateOf<Note?>(null) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.archive_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, stringResource(R.string.common_back)) }
                }
            )
        }
    ) { padding ->
        if (notes.isEmpty()) {
            EmptyState(
                icon = Icons.Outlined.Archive,
                title = stringResource(R.string.archive_empty_title),
                description = stringResource(R.string.archive_empty_desc),
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
                items(notes, key = { it.id }) { note ->
                    NoteItem(
                        note = note, 
                        onClick = { onNoteClick(note.id) },
                        onLongClick = { selectedNoteForMenu = note }
                    )
                }
            }
        }
    }

    if (selectedNoteForMenu != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedNoteForMenu = null },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp, top = 8.dp)) {
                ListItem(
                    headlineContent = { Text("Unarchive") },
                    leadingContent = { Icon(Icons.Outlined.Unarchive, null) },
                    modifier = Modifier.clickable {
                        val note = selectedNoteForMenu ?: return@clickable
                        viewModel.unarchiveNote(note)
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            selectedNoteForMenu = null
                        }
                    }
                )
                ListItem(
                    headlineContent = { Text(stringResource(R.string.common_delete), color = MaterialTheme.colorScheme.error) },
                    leadingContent = { Icon(Icons.Outlined.Delete, null, tint = MaterialTheme.colorScheme.error) },
                    modifier = Modifier.clickable {
                        val note = selectedNoteForMenu ?: return@clickable
                        viewModel.deleteNote(note)
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            selectedNoteForMenu = null
                        }
                    }
                )
            }
        }
    }
}
