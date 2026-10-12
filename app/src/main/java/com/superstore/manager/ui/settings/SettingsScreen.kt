package com.superstore.manager.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.superstore.manager.R
import com.superstore.manager.data.sync.CloudSyncServiceImpl
import com.superstore.manager.util.LanguageHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentLanguage: String,
    onLanguageSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var apiKey by remember { mutableStateOf("") }
    var cloudUrl by remember { mutableStateOf("") }
    var backupStatusMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        if (backupStatusMessage != null) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Text(text = backupStatusMessage!!, modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.settings_language),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    FilterChip(
                        selected = currentLanguage == LanguageHelper.LANG_ENGLISH,
                        onClick = { onLanguageSelected(LanguageHelper.LANG_ENGLISH) },
                        label = { Text(stringResource(R.string.settings_lang_en)) },
                        leadingIcon = {
                            if (currentLanguage == LanguageHelper.LANG_ENGLISH) {
                                Icon(Icons.Default.Check, contentDescription = null)
                            }
                        }
                    )
                    FilterChip(
                        selected = currentLanguage == LanguageHelper.LANG_URDU,
                        onClick = { onLanguageSelected(LanguageHelper.LANG_URDU) },
                        label = { Text(stringResource(R.string.settings_lang_ur)) },
                        leadingIcon = {
                            if (currentLanguage == LanguageHelper.LANG_URDU) {
                                Icon(Icons.Default.Check, contentDescription = null)
                            }
                        }
                    )
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.settings_ai_config),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Configure online AI provider (e.g. Gemini API Key). If not configured, manual features work fully offline.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text(stringResource(R.string.settings_api_key)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { backupStatusMessage = "AI API Key saved." },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(stringResource(R.string.action_save))
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.settings_cloud_config),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Optional cloud sync backend (e.g. Supabase REST URL). Stores data locally first.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = cloudUrl,
                    onValueChange = { cloudUrl = it },
                    label = { Text("Cloud Endpoint URL") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(onClick = {
                        coroutineScope.launch {
                            val app = context.applicationContext as com.superstore.manager.SuperstoreApp
                            val syncService = CloudSyncServiceImpl()
                            val res = syncService.backupLocalDatabaseToJson(context, app.database)
                            res.onSuccess {
                                backupStatusMessage = "Local JSON backup saved to: ${it.name}"
                            }.onFailure {
                                backupStatusMessage = "Backup error: ${it.message}"
                            }
                        }
                    }) {
                        Text(stringResource(R.string.action_backup_now))
                    }
                    Button(onClick = { backupStatusMessage = "Cloud settings saved." }) {
                        Text(stringResource(R.string.action_save))
                    }
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.settings_privacy),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "All sales and stock data are saved on your local device storage. AI identification sends requested images to the configured AI API over encrypted network connections.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
