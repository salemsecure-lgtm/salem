package dev.salemlift.app.onboarding

import dev.salemlift.domain.model.Experience
import dev.salemlift.domain.model.Split

/** Callbacks for the four onboarding steps. */
data class OnboardingActions(
    val onBack: () -> Unit,
    val onNext: () -> Unit,
    val onChooseExperience: (Experience) -> Unit,
    val onChooseDays: (Int) -> Unit,
    val onOverrideSplit: (Split) -> Unit,
    val onStart: () -> Unit,
)
