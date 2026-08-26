package com.example.offlinenotes.presentation.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.offlinenotes.domain.model.NoteVersion
import com.example.offlinenotes.presentation.components.EmptyState
import com.example.offlinenotes.presentation.components.PremiumCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    noteId: String,
    onNavigateBack: () -> Unit,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val versions by viewModel.versions.collectAsStateWithLifecycle()
    var showRestoreDialog by remember { mutableStateOf<NoteVersion?>(null) }

    LaunchedEffect(noteId) { viewModel.loadHistory(noteId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Version Timeline", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        if (versions.isEmpty()) {
            EmptyState(
                icon = Icons.Outlined.History,
                title = "No history yet",
                description = "Changes will appear here as you edit your notes.",
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(24.dp)
            ) {
                items(versions) { version ->
                    TimelineItem(
                        version = version,
                        isLast = version == versions.last(),
                        onRestore = { showRestoreDialog = version }
                    )
                }
            }
        }
    }

    if (showRestoreDialog != null) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = null },
            title = { Text("Restore Version") },
            text = { Text("Are you sure you want to restore this version? This will be added as a new entry in your timeline.") },
            confirmButton = {
                TextButton(onClick = {
                    showRestoreDialog?.let { viewModel.restore(noteId, it.id) }
                    showRestoreDialog = null
                    onNavigateBack()
                }) { Text("Restore") }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = null }) { Text("Cancel") }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }
}

@Composable
fun TimelineItem(
    version: NoteVersion,
    isLast: Boolean,
    onRestore: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(2.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.padding(bottom = 32.dp)) {
            val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
            Text(
                text = sdf.format(Date(version.savedAt)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            PremiumCard(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            if (version.title.isNotEmpty()) {
                                Text(
                                    text = version.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                            Text(
                                text = version.content,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                        IconButton(onClick = onRestore) {
                            Icon(
                                imageVector = Icons.Default.Restore,
                                contentDescription = "Restore",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}