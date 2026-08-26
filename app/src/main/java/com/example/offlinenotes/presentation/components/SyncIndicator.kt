package com.example.offlinenotes.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.offlinenotes.domain.model.SyncStatus
import com.example.offlinenotes.presentation.theme.Success

@Composable
fun SyncIndicator(
    status: SyncStatus,
    modifier: Modifier = Modifier
) {
    val icon = when (status) {
        SyncStatus.SYNCED -> Icons.Default.CloudDone
        SyncStatus.PENDING -> Icons.Default.CloudSync
        SyncStatus.ERROR, SyncStatus.CONFLICT -> Icons.Default.CloudOff
    }

    val color by animateColorAsState(
        targetValue = when (status) {
            SyncStatus.SYNCED -> Success
            SyncStatus.PENDING -> MaterialTheme.colorScheme.primary
            SyncStatus.ERROR, SyncStatus.CONFLICT -> MaterialTheme.colorScheme.error
        },
        label = "sync_color"
    )

    Icon(
        imageVector = icon,
        contentDescription = status.name,
        modifier = modifier.size(16.dp),
        tint = color.copy(alpha = 0.8f)
    )
}
