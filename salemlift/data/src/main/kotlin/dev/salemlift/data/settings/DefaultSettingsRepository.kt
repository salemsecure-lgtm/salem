package dev.salemlift.data.settings

import androidx.room.withTransaction
import dev.salemlift.data.db.RestPrefEntity
import dev.salemlift.data.db.RuleOverrideEntity
import dev.salemlift.data.db.SalemDatabase
import dev.salemlift.data.seedLandmarksIfEmpty
import dev.salemlift.data.toEntity
import dev.salemlift.data.toLandmarks
import dev.salemlift.domain.engine.AutoregRule
import dev.salemlift.domain.model.DefaultLandmarks
import dev.salemlift.domain.model.Experience
import dev.salemlift.domain.model.Landmarks
import dev.salemlift.domain.model.Muscle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * Room-backed [SettingsRepository]. Shares the landmark table with
 * [dev.salemlift.data.DefaultTrainingRepository]; the tuned rule table it
 * exposes is the exact table the tracker's commit path evaluates (see
 * [dev.salemlift.data.runAdvance], which loads the same overrides inside the
 * commit transaction).
 */
class DefaultSettingsRepository(
    private val database: SalemDatabase,
) : SettingsRepository {
    private val landmarkDao get() = database.landmarkDao()
    private val ruleOverrideDao get() = database.ruleOverrideDao()
    private val restPrefDao get() = database.restPrefDao()

    override fun landmarks(): Flow<Map<Muscle, Landmarks>> =
        flow {
            seedLandmarksIfEmpty(landmarkDao)
            emitAll(
                landmarkDao.observeAll().map { rows ->
                    rows.associate { it.muscle to it.toLandmarks() }
                },
            )
        }

    override suspend fun updateLandmarks(
        muscle: Muscle,
        landmarks: Landmarks,
    ) {
        database.withTransaction {
            seedLandmarksIfEmpty(landmarkDao)
            landmarkDao.upsertAll(listOf(landmarks.toEntity(muscle)))
        }
    }

    override suspend fun applyExperienceSeeds(experience: Experience) {
        database.withTransaction {
            landmarkDao.upsertAll(
                DefaultLandmarks.seedsFor(experience).map { (muscle, landmarks) -> landmarks.toEntity(muscle) },
            )
        }
    }

    override suspend fun ruleTable(): List<AutoregRule> =
        RuleTables.tuned(ruleOverrideDao.getAll().associate { it.ruleId to it.delta })

    override suspend fun updateRuleDelta(
        ruleId: String,
        delta: Int,
    ) {
        require(ruleId in RuleTables.editableRuleIds) {
            "rule $ruleId is not delta-editable (Fixed-delta rules only: ${RuleTables.editableRuleIds})"
        }
        require(delta in RuleTables.DELTA_RANGE) {
            "delta must be within ${RuleTables.DELTA_RANGE}, was $delta"
        }
        ruleOverrideDao.upsert(RuleOverrideEntity(ruleId = ruleId, delta = delta))
    }

    override suspend fun resetRules() {
        ruleOverrideDao.deleteAll()
    }

    override fun restSeconds(): Flow<Int> =
        restPrefDao.observe().map { it?.restSeconds ?: SettingsRepository.DEFAULT_REST_SECONDS }

    override suspend fun setRestSeconds(seconds: Int) {
        require(seconds in SettingsRepository.REST_SECONDS_RANGE) {
            "rest seconds must be within ${SettingsRepository.REST_SECONDS_RANGE}, was $seconds"
        }
        restPrefDao.upsert(RestPrefEntity(restSeconds = seconds))
    }

    override suspend fun exportBackup(): String =
        database.withTransaction {
            BackupCodec.encode(
                BackupDocument(
                    version = BackupCodec.VERSION,
                    landmarks = landmarkDao.getAll(),
                    ruleOverrides = ruleOverrideDao.getAll(),
                    restPref = restPrefDao.get(),
                    customExercises = database.exerciseDao().getCustom(),
                    mesocycles = database.mesocycleDao().getAll(),
                    plannedSessions = database.plannedSessionDao().getAll(),
                    sessionMuscleTargets = database.sessionMuscleTargetDao().getAll(),
                    muscleWeekStates = database.muscleWeekStateDao().getAll(),
                    loggedSets = database.loggedSetDao().getAll(),
                    muscleFeedback = database.muscleFeedbackDao().getAll(),
                    decisions = database.decisionDao().getAll(),
                ),
            )
        }

    override suspend fun importBackup(json: String) {
        val document = BackupCodec.decode(json)
        require(document.version == BackupCodec.VERSION) {
            "unsupported backup version ${document.version} (this build reads version ${BackupCodec.VERSION})"
        }
        // Rule overrides are re-validated on import: a hand-edited backup must
        // not smuggle values past updateRuleDelta's guard into the engine.
        document.ruleOverrides.forEach { override ->
            require(override.ruleId in RuleTables.editableRuleIds) {
                "backup contains an override for non-editable rule ${override.ruleId}"
            }
            require(override.delta in RuleTables.DELTA_RANGE) {
                "backup override for ${override.ruleId} has delta ${override.delta} outside ${RuleTables.DELTA_RANGE}"
            }
        }
        database.withTransaction {
            // Destructive replace: clear every backed-up table (mesocycle
            // cascades to all per-session children), then restore the
            // document's rows parents-first so foreign keys hold.
            database.mesocycleDao().deleteAll()
            landmarkDao.deleteAll()
            ruleOverrideDao.deleteAll()
            restPrefDao.deleteAll()
            database.exerciseDao().deleteCustom()

            landmarkDao.upsertAll(document.landmarks)
            ruleOverrideDao.upsertAll(document.ruleOverrides)
            document.restPref?.let { restPrefDao.upsert(it) }
            database.exerciseDao().insertAll(document.customExercises)
            database.mesocycleDao().insertAll(document.mesocycles)
            database.plannedSessionDao().insertAll(document.plannedSessions)
            database.sessionMuscleTargetDao().upsertAll(document.sessionMuscleTargets)
            database.muscleWeekStateDao().upsertAll(document.muscleWeekStates)
            database.loggedSetDao().insertAll(document.loggedSets)
            database.muscleFeedbackDao().upsertAll(document.muscleFeedback)
            database.decisionDao().upsertAll(document.decisions)
        }
    }
}
