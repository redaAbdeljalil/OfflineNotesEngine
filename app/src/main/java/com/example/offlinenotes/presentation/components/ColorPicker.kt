package com.example.offlinenotes.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.offlinenotes.presentation.theme.*

@Composable
fun ColorPicker(
    selectedColorHex: String?,
    onColorSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = listOf(
        null to NoteDefault,
        "#FEF3C7" to NoteWarm,
        "#DBEAFE" to NoteBlue,
        "#DCFCE7" to NoteGreen,
        "#F3E8FF" to NotePurple,
        "#FFE4E6" to NoteRose
    )

    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(colors) { (hex, color) ->
            val isSelected = selectedColorHex == hex
            val displayColor = if (color == NoteDefault) MaterialTheme.colorScheme.surfaceVariant else color

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(displayColor)
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        shape = CircleShape
                    )
                    .clickable { onColorSelected(hex) },
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                } else if (hex == null) {
                    Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = "Default",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
