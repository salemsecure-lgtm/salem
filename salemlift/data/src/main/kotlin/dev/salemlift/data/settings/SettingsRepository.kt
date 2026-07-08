package dev.salemlift.data.settings

import dev.salemlift.domain.engine.AutoregRule
import dev.salemlift.domain.model.Experience
import dev.salemlift.domain.model.Landmarks
import dev.salemlift.domain.model.Muscle
import kotlinx.coroutines.flow.Flow

/**
 * Persistence facade for user-tunable settings (DOMAIN.md §2 landmarks,
 * §5 rule deltas), the rest-timer preference, and local backup/export.
 * All suspend/Flow; implementations keep I/O off the main thread.
 */
interface SettingsRepository {
    /** Persist landmark seeds if absent, then observe them (shared landmark table). */
    fun landmarks(): Flow<Map<Muscle, Landmarks>>

    /**
     * Overwrite one muscle's landmarks. The MV ≤ MEV ≤ MAV ≤ MRV invariant is
     * enforced by [Landmarks] itself — an invalid combination cannot be built.
     */
    suspend fun updateLandmarks(
        muscle: Muscle,
        landmarks: Landmarks,
    )

    /**
     * Overwrite ALL landmark rows with
     * [dev.salemlift.domain.model.DefaultLandmarks.seedsFor] (DOMAIN.md §2.1).
     */
    suspend fun applyExperienceSeeds(experience: Experience)

    /**
     * The effective autoregulation table: [dev.salemlift.domain.engine.DefaultRules]
     * with Fixed-delta rows replaced by stored overrides. Conditions and
     * first-match-wins order are fixed in v1; R4's distance-scaled delta is
     * exposed read-only. The tracker's commit path evaluates this same table.
     */
    suspend fun ruleTable(): List<AutoregRule>

    /**
     * Store a delta override for a Fixed-delta rule.
     * @throws IllegalArgumentException for a non-editable/unknown rule id or a
     * delta outside [RuleTables.DELTA_RANGE].
     */
    suspend fun updateRuleDelta(
        ruleId: String,
        delta: Int,
    )

    /** Drop all delta overrides, restoring the shipped defaults. */
    suspend fun resetRules()

    /** The rest timer's default duration in seconds ([DEFAULT_REST_SECONDS] until set). */
    fun restSeconds(): Flow<Int>

    /** @throws IllegalArgumentException outside [REST_SECONDS_RANGE]. */
    suspend fun setRestSeconds(seconds: Int)

    /**
     * One versioned JSON document containing every user-data table (landmarks,
     * rule overrides, rest pref, mesocycles, planned sessions, targets, week
     * states, logged sets, feedback, decisions) plus custom exercises. The
     * seeded exercise catalog is NOT exported — it is re-seeded from assets.
     */
    suspend fun exportBackup(): String

    /**
     * Destructive replace: validates the document version, then swaps the
     * contents of every backed-up table for the document's rows in ONE
     * transaction. Existing user data is lost; the seeded catalog is kept.
     * On any failure the transaction rolls back and the database is unchanged.
     */
    suspend fun importBackup(json: String)

    companion object {
        const val DEFAULT_REST_SECONDS: Int = 150
        val REST_SECONDS_RANGE: IntRange = 30..600
    }
}
