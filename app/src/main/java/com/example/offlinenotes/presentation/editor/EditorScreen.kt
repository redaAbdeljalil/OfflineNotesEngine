package com.example.offlinenotes.presentation.editor

import androidx.compose.animation.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.offlinenotes.domain.model.EditorFont
import com.example.offlinenotes.presentation.components.ColorPicker
import com.example.offlinenotes.presentation.components.TagEditor
import com.example.offlinenotes.presentation.theme.getNoteColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    noteId: String,
    onNavigateBack: () -> Unit,
    onViewHistory: (String) -> Unit,
    viewModel: EditorViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showColorPicker by remember { mutableStateOf(false) }
    var showTagEditor by remember { mutableStateOf(false) }

    val isDark = isSystemInDarkTheme()
    val noteColor = getNoteColor(state.colorHex, isDark)
    
    val backgroundColor = if (noteColor == Color.Transparent) {
        MaterialTheme.colorScheme.background
    } else {
        noteColor
    }

    val contentColor = if (noteColor != Color.Transparent) {
        if (isDark) Color.White else Color.Black
    } else {
        MaterialTheme.colorScheme.onBackground
    }

    val fontFamily = when(state.editorFont) {
        EditorFont.SANS -> FontFamily.SansSerif
        EditorFont.SERIF -> FontFamily.Serif
        EditorFont.MONOSPACE -> FontFamily.Monospace
    }

    LaunchedEffect(noteId) {
        viewModel.loadNote(noteId)
    }

    Scaffold(
        containerColor = backgroundColor,
        contentColor = contentColor,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = contentColor
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Saving...", style = MaterialTheme.typography.labelSmall, color = contentColor)
                        } else {
                            Text(
                                text = if (state.isExisting) "Edit Note" else "New Note",
                                style = MaterialTheme.typography.titleMedium,
                                color = contentColor
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.saveImmediately()
                        onNavigateBack()
                    }) { Icon(Icons.Default.ArrowBack, "Back", tint = contentColor) }
                },
                actions = {
                    IconButton(onClick = viewModel::undo) { Icon(Icons.Default.Undo, "Undo", tint = contentColor) }
                    IconButton(onClick = viewModel::redo) { Icon(Icons.Default.Redo, "Redo", tint = contentColor) }
                    if (state.isExisting) {
                        IconButton(onClick = { onViewHistory(state.noteId) }) {
                            Icon(Icons.Outlined.History, "History", tint = contentColor)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            ) {
                IconButton(onClick = { 
                    showColorPicker = !showColorPicker
                    if (showColorPicker) showTagEditor = false
                }) {
                    Icon(Icons.Outlined.Palette, "Color")
                }
                IconButton(onClick = { 
                    showTagEditor = !showTagEditor
                    if (showTagEditor) showColorPicker = false
                }) {
                    Icon(Icons.Outlined.LocalOffer, "Tags")
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${state.content.length} characters",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(end = 16.dp)
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            AnimatedVisibility(visible = showColorPicker) {
                ColorPicker(
                    selectedColorHex = state.colorHex,
                    onColorSelected = {
                        viewModel.onColorChange(it)
                        showColorPicker = false
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            AnimatedVisibility(visible = showTagEditor) {
                TagEditor(
                    tags = state.tags,
                    onTagsChanged = viewModel::onTagsChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                TextField(
                    value = state.title,
                    onValueChange = viewModel::onTitleChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { 
                        Text("Title", style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = contentColor.copy(alpha = 0.3f),
                            fontFamily = fontFamily
                        )) 
                    },
                    textStyle = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = fontFamily,
                        color = contentColor
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = contentColor,
                        unfocusedTextColor = contentColor
                    )
                )
                
                TextField(
                    value = state.content,
                    onValueChange = viewModel::onContentChange,
                    modifier = Modifier.fillMaxSize(),
                    placeholder = { 
                        Text("Share your thoughts...", style = MaterialTheme.typography.bodyLarge.copy(
                            color = contentColor.copy(alpha = 0.3f),
                            fontFamily = fontFamily
                        )) 
                    },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        lineHeight = 28.sp,
                        fontFamily = fontFamily,
                        color = contentColor
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = contentColor,
                        unfocusedTextColor = contentColor
                    )
                )
            }
        }
    }
}
