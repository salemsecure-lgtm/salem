package dev.salemlift.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import dev.salemlift.data.SessionState
import dev.salemlift.domain.model.ClampBound
import dev.salemlift.domain.model.JointPain
import dev.salemlift.domain.model.Muscle
import dev.salemlift.domain.model.Performance
import dev.salemlift.domain.model.Pump
import dev.salemlift.domain.model.Soreness
import kotlinx.serialization.Serializable

/** Lifecycle of a persisted mesocycle. */
enum class MesoState { ACTIVE, COMPLETED }

// All tracker entities are @Serializable so the settings backup/export can
// embed the rows verbatim (see dev.salemlift.data.settings.BackupCodec); the
// backup document is versioned, so entity/format changes bump that version.

/**
 * Per-muscle weekly volume landmarks (DOMAIN.md §2), seeded from
 * [dev.salemlift.domain.model.DefaultLandmarks] on first read and user-tunable later.
 */
@Serializable
@Entity(tableName = "landmark")
data class LandmarkEntity(
    @PrimaryKey val muscle: Muscle,
    val mv: Int,
    val mev: Int,
    val mav: Int,
    val mrv: Int,
)

/**
 * One instantiated mesocycle (DOMAIN.md §3). [splitJson] holds the full split
 * (kotlinx-serialization, see [TrackerCodecs]) so distributions can be re-projected
 * onto session slots after feedback commits.
 */
@Serializable
@Entity(tableName = "mesocycle")
data class MesocycleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startedAtEpochMillis: Long,
    val accumulationWeeks: Int,
    val deloadLoadMultiplier: Double,
    val splitJson: String,
    val state: MesoState = MesoState.ACTIVE,
)

/** One planned training day of a mesocycle, with its effort prescription (DOMAIN.md §3.1). */
@Serializable
@Entity(
    tableName = "planned_session",
    foreignKeys = [
        ForeignKey(
            entity = MesocycleEntity::class,
            parentColumns = ["id"],
            childColumns = ["mesoId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("mesoId")],
)
data class PlannedSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mesoId: Long,
    /** 1-based week within the mesocycle. */
    val week: Int,
    /** 0-based position within the week — identical to the split's session index. */
    val dayIndex: Int,
    val name: String,
    val isDeload: Boolean,
    val targetRir: Int,
    val allowZeroOnLastSet: Boolean,
    val maxRir: Int,
    val loadMultiplier: Double,
    val state: SessionState = SessionState.PENDING,
    val completedAtEpochMillis: Long? = null,
)

/** Prescribed hard sets for one muscle in one planned session (zero-set muscles have no row). */
@Serializable
@Entity(
    tableName = "session_muscle_target",
    primaryKeys = ["sessionId", "muscle"],
    foreignKeys = [
        ForeignKey(
            entity = PlannedSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("sessionId")],
)
data class SessionMuscleTargetEntity(
    val sessionId: Long,
    val muscle: Muscle,
    val sets: Int,
)

/**
 * A muscle's live weekly state while its mesocycle runs
 * (mirrors [dev.salemlift.domain.model.MuscleWeekState]; DOMAIN.md §5).
 */
@Serializable
@Entity(
    tableName = "muscle_week_state",
    primaryKeys = ["mesoId", "muscle"],
    foreignKeys = [
        ForeignKey(
            entity = MesocycleEntity::class,
            parentColumns = ["id"],
            childColumns = ["mesoId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("mesoId")],
)
data class MuscleWeekStateEntity(
    val mesoId: Long,
    val muscle: Muscle,
    val weeklySets: Int,
    /** JSON int array — per-slot sets across the sessions training the muscle (see [TrackerCodecs]). */
    val distributionJson: String,
    val stallCount: Int,
)

/** One logged working set (DOMAIN.md §1 "hard set"). */
@Serializable
@Entity(
    tableName = "logged_set",
    foreignKeys = [
        ForeignKey(
            entity = PlannedSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("sessionId")],
)
data class LoggedSetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val exerciseId: String,
    val muscle: Muscle,
    val weightKg: Double,
    val reps: Int,
    val rir: Int,
    val orderInSession: Int,
    val loggedAtEpochMillis: Long,
)

/** Per-muscle feedback captured for one committed session (DOMAIN.md §4). */
@Serializable
@Entity(
    tableName = "muscle_feedback",
    primaryKeys = ["sessionId", "muscle"],
    foreignKeys = [
        ForeignKey(
            entity = PlannedSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("sessionId")],
)
data class MuscleFeedbackEntity(
    val sessionId: Long,
    val muscle: Muscle,
    val soreness: Soreness,
    val pump: Pump,
    val jointPain: JointPain,
    val performance: Performance,
)

/**
 * The engine's explainable per-muscle output for one committed session
 * (mirrors [dev.salemlift.domain.model.SetDecision]; DOMAIN.md §5).
 */
@Serializable
@Entity(
    tableName = "decision",
    primaryKeys = ["sessionId", "muscle"],
    foreignKeys = [
        ForeignKey(
            entity = PlannedSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("sessionId")],
)
data class DecisionEntity(
    val sessionId: Long,
    val muscle: Muscle,
    val ruleId: String,
    val rationale: String,
    val rawDelta: Int,
    val cappedDelta: Int,
    val previousSets: Int,
    val nextSets: Int,
    val mildPainCapped: Boolean,
    val clampedAt: ClampBound?,
    val exerciseSwapFlagged: Boolean,
)
