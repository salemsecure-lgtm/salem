package dev.salemlift.app.settings

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.salemlift.app.di.AppContainer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val BACKUP_MIME = "application/json"
private const val BACKUP_FILE_NAME = "salemlift-backup.json"

/**
 * Hosts the settings ViewModel and the SAF document launchers. The launchers
 * hand suspend read/write lambdas to the ViewModel; only the stream copy runs
 * on [Dispatchers.IO]. SAF needs no permissions.
 */
@Composable
fun SettingsRoute(
    container: AppContainer,
    onBack: () -> Unit,
) {
    val viewModel: SettingsViewModel = viewModel { SettingsViewModel(container.settingsRepository) }
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is SettingsViewModel.Event.Message -> snackbarHostState.showSnackbar(event.text)
            }
        }
    }

    val exportLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(BACKUP_MIME)) { uri ->
            if (uri != null) {
                viewModel.exportTo { json ->
                    withContext(Dispatchers.IO) {
                        val stream = context.contentResolver.openOutputStream(uri) ?: error("cannot write $uri")
                        stream.use { it.write(json.encodeToByteArray()) }
                    }
                }
            }
        }
    val importLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                viewModel.importFrom {
                    withContext(Dispatchers.IO) {
                        val stream = context.contentResolver.openInputStream(uri) ?: error("cannot read $uri")
                        stream.use { it.readBytes().decodeToString() }
                    }
                }
            }
        }

    SettingsScreen(
        state = state,
        appVersion = remember { appVersion(context) },
        snackbarHostState = snackbarHostState,
        actions =
            SettingsActions(
                onBack = onBack,
                onOpenLandmark = viewModel::openLandmarkEditor,
                onEditorAdjust = viewModel::adjustEditor,
                onEditorSave = viewModel::saveLandmarkEditor,
                onEditorDismiss = viewModel::dismissLandmarkEditor,
                onApplyExperience = viewModel::applyExperienceSeeds,
                onAdjustRuleDelta = viewModel::adjustRuleDelta,
                onResetRules = viewModel::resetRules,
                onAdjustRestSeconds = viewModel::adjustRestSeconds,
                onExport = { exportLauncher.launch(BACKUP_FILE_NAME) },
                onImport = { importLauncher.launch(arrayOf(BACKUP_MIME)) },
                onConfirmImport = viewModel::confirmImport,
                onCancelImport = viewModel::cancelImport,
            ),
    )
}

private fun appVersion(context: Context): String =
    runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }
        .getOrNull() ?: "unknown"
