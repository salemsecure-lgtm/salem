package dev.salemlift.data.settings

import dev.salemlift.data.db.DecisionEntity
import dev.salemlift.data.db.ExerciseEntity
import dev.salemlift.data.db.LandmarkEntity
import dev.salemlift.data.db.LoggedSetEntity
import dev.salemlift.data.db.MesocycleEntity
import dev.salemlift.data.db.MuscleFeedbackEntity
import dev.salemlift.data.db.MuscleWeekStateEntity
import dev.salemlift.data.db.PlannedSessionEntity
import dev.salemlift.data.db.RestPrefEntity
import dev.salemlift.data.db.RuleOverrideEntity
import dev.salemlift.data.db.SessionMuscleTargetEntity
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * The versioned backup document: a verbatim snapshot of every user-data table.
 * The seeded exercise catalog is deliberately absent — only user-created
 * custom exercises (isCustom = true) are carried; the catalog is re-seeded
 * from bundled assets on any device.
 */
@Serializable
data class BackupDocument(
    val version: Int,
    val landmarks: List<LandmarkEntity> = emptyList(),
    val ruleOverrides: List<RuleOverrideEntity> = emptyList(),
    val restPref: RestPrefEntity? = null,
    val customExercises: List<ExerciseEntity> = emptyList(),
    val mesocycles: List<MesocycleEntity> = emptyList(),
    val plannedSessions: List<PlannedSessionEntity> = emptyList(),
    val sessionMuscleTargets: List<SessionMuscleTargetEntity> = emptyList(),
    val muscleWeekStates: List<MuscleWeekStateEntity> = emptyList(),
    val loggedSets: List<LoggedSetEntity> = emptyList(),
    val muscleFeedback: List<MuscleFeedbackEntity> = emptyList(),
    val decisions: List<DecisionEntity> = emptyList(),
)

/** JSON codec for [BackupDocument]. Bump [VERSION] whenever the row shapes change. */
object BackupCodec {
    const val VERSION: Int = 1

    private val json =
        Json {
            // Full rows always, so a document is self-contained even for default values.
            encodeDefaults = true
        }

    fun encode(document: BackupDocument): String = json.encodeToString(BackupDocument.serializer(), document)

    /** @throws kotlinx.serialization.SerializationException on malformed input. */
    fun decode(raw: String): BackupDocument = json.decodeFromString(BackupDocument.serializer(), raw)
}
