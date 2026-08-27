package com.example.offlinenotes.presentation.notes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.offlinenotes.domain.model.Note
import com.example.offlinenotes.presentation.components.EmptyState
import com.example.offlinenotes.presentation.components.NoteItem
import androidx.compose.ui.res.stringResource
import com.example.offlinenotes.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNoteClick: (String) -> Unit,
    onCreateNote: () -> Unit,
    onSettingsClick: () -> Unit,
    onArchiveClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    
    var selectedNoteForMenu by remember { mutableStateOf<Note?>(null) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.home_title),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp
                        )
                    )
                },
                actions = {
                    IconButton(onClick = onArchiveClick) {
                        Icon(Icons.Outlined.Archive, contentDescription = stringResource(R.string.archive_title))
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Outlined.Settings, contentDescription = stringResource(R.string.settings_title))
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                ),
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateNote,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.home_new_note)) },
                shape = RoundedCornerShape(20.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            SearchBar(
                query = uiState.searchQuery,
                onQueryChange = viewModel::updateSearchQuery,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (uiState.notes.isEmpty()) {
                val emptyTitle = if (uiState.searchQuery.isEmpty()) 
                    stringResource(R.string.home_no_notes_title) 
                else stringResource(R.string.home_no_results_title)
                
                val emptyDesc = if (uiState.searchQuery.isEmpty()) 
                    stringResource(R.string.home_no_notes_desc)
                else stringResource(R.string.home_no_results_desc, uiState.searchQuery)
                
                EmptyState(
                    icon = if (uiState.searchQuery.isEmpty()) Icons.Default.NoteAdd else Icons.Default.SearchOff,
                    title = emptyTitle,
                    description = emptyDesc
                )
            } else {
                val pinnedNotes = uiState.notes.filter { it.isPinned }
                val otherNotes = uiState.notes.filter { !it.isPinned }

                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalItemSpacing = 12.dp,
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (pinnedNotes.isNotEmpty()) {
                        item(span = StaggeredGridItemSpan.FullLine) {
                            SectionHeader(stringResource(R.string.home_section_pinned))
                        }
                        items(pinnedNotes, key = { it.id }) { note ->
                            NoteItem(
                                note = note, 
                                onClick = { onNoteClick(note.id) },
                                onLongClick = { selectedNoteForMenu = note }
                            )
                        }
                        item(span = StaggeredGridItemSpan.FullLine) {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    if (otherNotes.isNotEmpty()) {
                        item(span = StaggeredGridItemSpan.FullLine) {
                            SectionHeader(if (pinnedNotes.isEmpty()) 
                                stringResource(R.string.home_section_all) 
                            else stringResource(R.string.home_section_recent))
                        }
                        items(otherNotes, key = { it.id }) { note ->
                            NoteItem(
                                note = note, 
                                onClick = { onNoteClick(note.id) },
                                onLongClick = { selectedNoteForMenu = note }
                            )
                        }
                    }
                }
            }
        }
    }

    if (selectedNoteForMenu != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedNoteForMenu = null },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp, top = 8.dp)
            ) {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.common_edit)) },
                    leadingContent = { Icon(Icons.Outlined.Edit, null) },
                    modifier = Modifier.clickable {
                        val note = selectedNoteForMenu ?: return@clickable
                        onNoteClick(note.id)
                        selectedNoteForMenu = null
                    }
                )
                ListItem(
                    headlineContent = { Text(stringResource(R.string.common_archive)) },
                    leadingContent = { Icon(Icons.Outlined.Archive, null) },
                    modifier = Modifier.clickable {
                        val note = selectedNoteForMenu ?: return@clickable
                        viewModel.archiveNote(note)
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

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.heightIn(min = 56.dp),
        placeholder = { Text(stringResource(R.string.home_search_placeholder), style = MaterialTheme.typography.bodyLarge) },
        leadingIcon = { Icon(Icons.Default.Search, null, tint = MaterialTheme.colorScheme.primary) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, null)
                }
            }
        },
        shape = RoundedCornerShape(28.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        singleLine = true
    )
}