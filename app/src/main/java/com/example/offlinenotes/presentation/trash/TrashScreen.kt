package com.example.offlinenotes.presentation.trash

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.RestoreFromTrash
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
fun TrashScreen(
    onNavigateBack: () -> Unit,
    viewModel: TrashViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedNoteForMenu by remember { mutableStateOf<Note?>(null) }
    var showEmptyConfirm by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.trash_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, stringResource(R.string.common_back)) }
                },
                actions = {
                    if (uiState.notes.isNotEmpty()) {
                        IconButton(onClick = { showEmptyConfirm = true }) {
                            Icon(Icons.Default.DeleteSweep, stringResource(R.string.trash_dialog_empty_title))
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.notes.isEmpty()) {
            EmptyState(
                icon = Icons.Outlined.Delete,
                title = stringResource(R.string.trash_empty_title),
                description = stringResource(R.string.trash_empty_desc),
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
                items(uiState.notes, key = { it.id }) { note ->
                    NoteItem(
                        note = note, 
                        onClick = { selectedNoteForMenu = note },
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
                    headlineContent = { Text(stringResource(R.string.common_restore)) },
                    leadingContent = { Icon(Icons.Outlined.RestoreFromTrash, null) },
                    modifier = Modifier.clickable {
                        val note = selectedNoteForMenu ?: return@clickable
                        viewModel.restoreNote(note)
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            selectedNoteForMenu = null
                        }
                    }
                )
                ListItem(
                    headlineContent = { Text(stringResource(R.string.common_delete_permanently), color = MaterialTheme.colorScheme.error) },
                    leadingContent = { Icon(Icons.Outlined.Delete, null, tint = MaterialTheme.colorScheme.error) },
                    modifier = Modifier.clickable {
                        val note = selectedNoteForMenu ?: return@clickable
                        viewModel.deleteNotePermanently(note.id)
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            selectedNoteForMenu = null
                        }
                    }
                )
            }
        }
    }

    if (showEmptyConfirm) {
        AlertDialog(
            onDismissRequest = { showEmptyConfirm = false },
            title = { Text(stringResource(R.string.trash_dialog_empty_title)) },
            text = { Text(stringResource(R.string.trash_dialog_empty_desc)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.emptyTrash()
                    showEmptyConfirm = false
                }) { Text(stringResource(R.string.common_delete), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showEmptyConfirm = false }) { Text(stringResource(R.string.common_cancel)) }
            }
        )
    }
}
