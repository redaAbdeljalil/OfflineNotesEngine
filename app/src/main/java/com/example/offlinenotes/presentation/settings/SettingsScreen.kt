package com.example.offlinenotes.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.offlinenotes.R
import com.example.offlinenotes.domain.model.AppTheme
import com.example.offlinenotes.domain.model.EditorFont
import com.example.offlinenotes.domain.model.NoteSorting
import com.example.offlinenotes.presentation.components.ColorPicker
import com.example.offlinenotes.presentation.components.PremiumCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onTrashClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsStateWithLifecycle()
    val isScreenshotProtected by viewModel.isScreenshotProtected.collectAsStateWithLifecycle()
    
    var showThemeDialog by remember { mutableStateOf(false) }
    var showSortingDialog by remember { mutableStateOf(false) }
    var showFontDialog by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, stringResource(R.string.common_back)) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SettingsSection(stringResource(R.string.settings_section_appearance)) {
                    SettingsItem(
                        icon = Icons.Outlined.DarkMode,
                        title = stringResource(R.string.settings_item_dark_mode),
                        description = settings.theme.name.lowercase().replaceFirstChar { it.uppercase() },
                        onClick = { showThemeDialog = true }
                    )
                    SettingsItem(
                        icon = Icons.Outlined.Palette,
                        title = stringResource(R.string.settings_item_default_color),
                        description = stringResource(R.string.settings_item_default_color_desc),
                        onClick = { showColorPicker = !showColorPicker }
                    )
                    if (showColorPicker) {
                        ColorPicker(
                            selectedColorHex = settings.defaultColorHex,
                            onColorSelected = { viewModel.setDefaultColor(it) }
                        )
                    }
                }
            }
            item {
                SettingsSection(stringResource(R.string.settings_section_security)) {
                    SettingsToggleItem(
                        icon = Icons.Outlined.Fingerprint,
                        title = stringResource(R.string.settings_item_biometric),
                        description = stringResource(R.string.settings_item_biometric_desc),
                        checked = isBiometricEnabled,
                        onCheckedChange = viewModel::setBiometricEnabled
                    )
                    SettingsToggleItem(
                        icon = Icons.Outlined.Security,
                        title = stringResource(R.string.settings_item_screenshot),
                        description = stringResource(R.string.settings_item_screenshot_desc),
                        checked = isScreenshotProtected,
                        onCheckedChange = viewModel::setScreenshotProtection
                    )
                }
            }
            item {
                SettingsSection(stringResource(R.string.settings_section_notes)) {
                    SettingsItem(
                        icon = Icons.Outlined.Sort,
                        title = stringResource(R.string.settings_item_sorting),
                        description = when(settings.defaultSorting) {
                            NoteSorting.UPDATED -> "Recently updated"
                            NoteSorting.CREATED -> "Recently created"
                            NoteSorting.ALPHABETICAL -> "Alphabetical"
                            NoteSorting.PINNED -> "Pinned first"
                        },
                        onClick = { showSortingDialog = true }
                    )
                    SettingsItem(
                        icon = Icons.Outlined.TextFields,
                        title = stringResource(R.string.settings_item_font),
                        description = settings.editorFont.name.lowercase().replaceFirstChar { it.uppercase() },
                        onClick = { showFontDialog = true }
                    )
                }
            }
            item {
                SettingsSection(stringResource(R.string.settings_section_sync)) {
                    SettingsItem(
                        icon = if (settings.syncEnabled) Icons.Outlined.Sync else Icons.Outlined.SyncDisabled,
                        title = stringResource(R.string.settings_item_sync),
                        description = if (settings.syncEnabled) 
                            stringResource(R.string.settings_item_sync_on) 
                        else stringResource(R.string.settings_item_sync_off),
                        onClick = { viewModel.setSyncEnabled(!settings.syncEnabled) }
                    )
                    SettingsItem(
                        icon = Icons.Outlined.Delete,
                        title = stringResource(R.string.settings_item_trash),
                        description = stringResource(R.string.settings_item_trash_desc),
                        onClick = onTrashClick
                    )
                }
            }
            item {
                SettingsSection(stringResource(R.string.settings_section_about)) {
                    SettingsItem(Icons.Outlined.Info, stringResource(R.string.settings_item_version), "1.0.0-premium")
                    SettingsItem(
                        icon = Icons.Outlined.HelpOutline,
                        title = stringResource(R.string.settings_item_show_onboarding),
                        description = stringResource(R.string.settings_item_show_onboarding_desc),
                        onClick = { viewModel.completeOnboarding(false) }
                    )
                }
            }
        }
    }

    if (showThemeDialog) {
        SettingsSelectionDialog(
            title = "Choose Theme",
            options = AppTheme.values().toList(),
            selectedOption = settings.theme,
            onOptionSelected = { viewModel.setTheme(it); showThemeDialog = false },
            onDismiss = { showThemeDialog = false }
        )
    }

    if (showSortingDialog) {
        SettingsSelectionDialog(
            title = "Default Sorting",
            options = NoteSorting.values().toList(),
            selectedOption = settings.defaultSorting,
            onOptionSelected = { viewModel.setSorting(it); showSortingDialog = false },
            onDismiss = { showSortingDialog = false }
        )
    }

    if (showFontDialog) {
        SettingsSelectionDialog(
            title = "Editor Font",
            options = EditorFont.values().toList(),
            selectedOption = settings.editorFont,
            onOptionSelected = { viewModel.setFont(it); showFontDialog = false },
            onDismiss = { showFontDialog = false }
        )
    }
}

@Composable
fun <T> SettingsSelectionDialog(
    title: String,
    options: List<T>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOptionSelected(option) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = option == selectedOption,
                            onClick = null
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = option.toString().lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) }
        },
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
        )
        PremiumCard(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(content = content)
        }
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: (() -> Unit)? = null
) {
    Surface(
        onClick = onClick ?: {},
        color = androidx.compose.ui.graphics.Color.Transparent,
        enabled = onClick != null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            if (onClick != null) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }
        }
    }
}

@Composable
fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}
