package dev.salemlift.app.settings

import dev.salemlift.domain.model.Experience
import dev.salemlift.domain.model.Muscle

/** UI callbacks for the settings screen, grouped to keep composable signatures small. */
data class SettingsActions(
    val onBack: () -> Unit,
    val onOpenLandmark: (Muscle) -> Unit,
    val onEditorAdjust: (SettingsViewModel.LandmarkField, Int) -> Unit,
    val onEditorSave: () -> Unit,
    val onEditorDismiss: () -> Unit,
    val onApplyExperience: (Experience) -> Unit,
    val onAdjustRuleDelta: (String, Int) -> Unit,
    val onResetRules: () -> Unit,
    val onAdjustRestSeconds: (Int) -> Unit,
    val onExport: () -> Unit,
    val onImport: () -> Unit,
    val onConfirmImport: () -> Unit,
    val onCancelImport: () -> Unit,
)
