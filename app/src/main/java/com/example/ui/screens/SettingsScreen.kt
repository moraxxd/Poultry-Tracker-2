package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.entity.DailyRecord
import com.example.data.entity.Farm
import com.example.ui.theme.ForestGreenPrimary
import com.example.ui.viewmodel.FarmViewModel
import com.example.util.BackupManager
import com.example.util.DateUtils
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: FarmViewModel,
    allRecords: List<DailyRecord>,
    onResetCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val farm by viewModel.farm.collectAsStateWithLifecycle()
    val language by viewModel.language.collectAsStateWithLifecycle()

    var showEditNameDialog by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf("") }

    var showLanguageDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    var pendingRestoreJson by remember { mutableStateOf<String?>(null) }
    var showRestoreConfirmDialog by remember { mutableStateOf(false) }

    // SAF Document Launchers
    val exportCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        if (uri != null) {
            val csv = BackupManager.generateRecordsCsv(allRecords)
            val res = BackupManager.writeToUri(context, uri, csv)
            scope.launch {
                if (res.isSuccess) {
                    snackbarHostState.showSnackbar("CSV saved successfully.")
                } else {
                    snackbarHostState.showSnackbar("Failed to write CSV.")
                }
            }
        }
    }

    val backupJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null && farm != null) {
            val json = BackupManager.generateBackupJson(farm!!, allRecords)
            val res = BackupManager.writeToUri(context, uri, json)
            scope.launch {
                if (res.isSuccess) {
                    snackbarHostState.showSnackbar("Backup created successfully.")
                } else {
                    snackbarHostState.showSnackbar("Failed to create backup.")
                }
            }
        }
    }

    val restorePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val res = BackupManager.readFromUri(context, uri)
            if (res.isSuccess) {
                val jsonContent = res.getOrThrow()
                // Validate before presenting confirmation
                val validateResult = BackupManager.parseAndValidateBackup(jsonContent)
                if (validateResult.isSuccess) {
                    pendingRestoreJson = jsonContent
                    showRestoreConfirmDialog = true
                } else {
                    scope.launch {
                        snackbarHostState.showSnackbar("Invalid backup file: ${validateResult.exceptionOrNull()?.message}")
                    }
                }
            } else {
                scope.launch {
                    snackbarHostState.showSnackbar("Failed to read selected file.")
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = ForestGreenPrimary
                )
            }

            // Section 1: Farm Profile
            item {
                Text(
                    text = stringResource(R.string.settings_farm_profile),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ForestGreenPrimary
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Edit Farm Name Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    editedName = farm?.name ?: ""
                                    showEditNameDialog = true
                                }
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.settings_edit_name),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = farm?.name ?: "",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = ForestGreenPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Icon(Icons.Default.Edit, contentDescription = "Edit Name", tint = ForestGreenPrimary)
                        }

                        Spacer(Modifier.height(8.dp))

                        // Language Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showLanguageDialog = true }
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.settings_language),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = if (language == "hi") stringResource(R.string.settings_lang_hi) else stringResource(R.string.settings_lang_en),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = ForestGreenPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Icon(Icons.Default.Language, contentDescription = "Change Language", tint = ForestGreenPrimary)
                        }

                        Spacer(Modifier.height(8.dp))

                        // Initial Opening Inventory Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.settings_initial_inventory),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = if (farm != null) {
                                        stringResource(
                                            R.string.settings_initial_summary,
                                            farm!!.initialAdultChickens,
                                            farm!!.initialChicks,
                                            DateUtils.formatDateForDisplay(farm!!.initialDate)
                                        )
                                    } else {
                                        "None"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Section 2: Data & Backup
            item {
                Text(
                    text = stringResource(R.string.settings_data_backup),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ForestGreenPrimary
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Export CSV
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val fileName = "poultry_records_${DateUtils.getCurrentDateString()}.csv"
                                    exportCsvLauncher.launch(fileName)
                                }
                                .padding(vertical = 12.dp)
                                .testTag("btn_export_csv_saf"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.TableChart, contentDescription = null, tint = ForestGreenPrimary)
                            Spacer(Modifier.width(14.dp))
                            Text(
                                text = stringResource(R.string.settings_export_csv),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Share CSV via Sheet
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val csv = BackupManager.generateRecordsCsv(allRecords)
                                    BackupManager.shareContent(
                                        context,
                                        "poultry_records_${DateUtils.getCurrentDateString()}.csv",
                                        "text/csv",
                                        csv
                                    )
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, tint = ForestGreenPrimary)
                            Spacer(Modifier.width(14.dp))
                            Text(
                                text = "Share Records CSV (Share Sheet)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Full JSON Backup
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val fileName = "poultry_backup_${DateUtils.getCurrentDateString()}.json"
                                    backupJsonLauncher.launch(fileName)
                                }
                                .padding(vertical = 12.dp)
                                .testTag("btn_backup_full_saf"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Backup, contentDescription = null, tint = ForestGreenPrimary)
                            Spacer(Modifier.width(14.dp))
                            Text(
                                text = stringResource(R.string.settings_backup_full),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Restore Backup
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    restorePickerLauncher.launch(arrayOf("application/json", "*/*"))
                                }
                                .padding(vertical = 12.dp)
                                .testTag("btn_restore_saf"),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Restore, contentDescription = null, tint = ForestGreenPrimary)
                            Spacer(Modifier.width(14.dp))
                            Text(
                                text = stringResource(R.string.settings_restore_backup),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Section 3: Help & Rules
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showHelpDialog = true }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.HelpOutline, contentDescription = null, tint = ForestGreenPrimary)
                            Spacer(Modifier.width(14.dp))
                            Text(
                                text = stringResource(R.string.settings_help_rules),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Section 4: App Info & Danger Zone
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = ForestGreenPrimary)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.settings_app_info),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = stringResource(R.string.settings_version),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(R.string.settings_offline_note),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )

                        Spacer(Modifier.height(16.dp))

                        // Reset All Data Button
                        Button(
                            onClick = { showResetDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("btn_reset_all_data")
                        ) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.settings_reset_all))
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(80.dp))
            }
        }
    }

    // Edit Name Dialog
    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text(stringResource(R.string.settings_edit_name)) },
            text = {
                OutlinedTextField(
                    value = editedName,
                    onValueChange = { editedName = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (editedName.isNotBlank()) {
                            viewModel.updateFarmName(editedName)
                            showEditNameDialog = false
                        }
                    }
                ) {
                    Text(stringResource(R.string.dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            }
        )
    }

    // Language Selector Dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.settings_language)) },
            text = {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.setLanguage("en")
                                showLanguageDialog = false
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = language == "en", onClick = {
                            viewModel.setLanguage("en")
                            showLanguageDialog = false
                        })
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.settings_lang_en))
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.setLanguage("hi")
                                showLanguageDialog = false
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = language == "hi", onClick = {
                            viewModel.setLanguage("hi")
                            showLanguageDialog = false
                        })
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.settings_lang_hi))
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            }
        )
    }

    // Help Dialog
    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text(stringResource(R.string.help_title)) },
            text = {
                Text(
                    text = stringResource(R.string.help_content),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = { showHelpDialog = false }) {
                    Text(stringResource(R.string.dialog_confirm))
                }
            }
        )
    }

    // Reset Data Confirmation Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(stringResource(R.string.settings_reset_confirm_title)) },
            text = { Text(stringResource(R.string.settings_reset_confirm_msg)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetDialog = false
                        viewModel.resetAllData {
                            onResetCompleted()
                        }
                    }
                ) {
                    Text(
                        stringResource(R.string.dialog_confirm),
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            }
        )
    }

    // Restore Confirmation Dialog
    if (showRestoreConfirmDialog && pendingRestoreJson != null) {
        AlertDialog(
            onDismissRequest = {
                showRestoreConfirmDialog = false
                pendingRestoreJson = null
            },
            title = { Text(stringResource(R.string.msg_restore_confirm_title)) },
            text = { Text(stringResource(R.string.msg_restore_confirm_msg)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRestoreConfirmDialog = false
                        val json = pendingRestoreJson ?: return@TextButton
                        viewModel.restoreFromJson(json) { success, errorMsg ->
                            scope.launch {
                                if (success) {
                                    snackbarHostState.showSnackbar("Farm data restored successfully!")
                                } else {
                                    snackbarHostState.showSnackbar("Restore failed: $errorMsg")
                                }
                            }
                        }
                        pendingRestoreJson = null
                    }
                ) {
                    Text(
                        stringResource(R.string.dialog_confirm),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRestoreConfirmDialog = false
                    pendingRestoreJson = null
                }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            }
        )
    }
}
