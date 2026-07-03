package dev.salemlift.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.salemlift.domain.model.Muscle
import kotlinx.coroutines.flow.Flow

/** Volume-landmark rows (seeded once, then user-tunable). */
@Dao
interface LandmarkDao {
    @Query("SELECT COUNT(*) FROM landmark")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(rows: List<LandmarkEntity>)

    @Query("SELECT * FROM landmark")
    fun observeAll(): Flow<List<LandmarkEntity>>

    @Query("SELECT * FROM landmark")
    suspend fun getAll(): List<LandmarkEntity>
}

/** Mesocycle lifecycle queries. At most one row is ACTIVE at a time. */
@Dao
interface MesocycleDao {
    @Insert
    suspend fun insert(meso: MesocycleEntity): Long

    @Query("UPDATE mesocycle SET state = 'COMPLETED' WHERE state = 'ACTIVE'")
    suspend fun completeAllActive()

    @Query("UPDATE mesocycle SET state = 'COMPLETED' WHERE id = :id")
    suspend fun markCompleted(id: Long)

    @Query("SELECT * FROM mesocycle WHERE state = 'ACTIVE' ORDER BY id DESC LIMIT 1")
    suspend fun getActive(): MesocycleEntity?

    @Query("SELECT * FROM mesocycle WHERE state = 'ACTIVE' ORDER BY id DESC LIMIT 1")
    fun observeActive(): Flow<MesocycleEntity?>

    @Query("SELECT * FROM mesocycle WHERE id = :id")
    suspend fun getById(id: Long): MesocycleEntity?
}

/** Planned-session queries; "current" = first pending/in-progress day in (week, dayIndex) order. */
@Dao
interface PlannedSessionDao {
    @Insert
    suspend fun insert(session: PlannedSessionEntity): Long

    @Query("SELECT * FROM planned_session WHERE id = :id")
    suspend fun getById(id: Long): PlannedSessionEntity?

    @Query(
        "SELECT * FROM planned_session WHERE mesoId = :mesoId AND state IN ('PENDING', 'IN_PROGRESS') " +
            "ORDER BY week, dayIndex LIMIT 1",
    )
    fun observeCurrent(mesoId: Long): Flow<PlannedSessionEntity?>

    @Query(
        "SELECT * FROM planned_session WHERE mesoId = :mesoId AND state IN ('PENDING', 'IN_PROGRESS') " +
            "ORDER BY week, dayIndex LIMIT 1",
    )
    suspend fun getCurrent(mesoId: Long): PlannedSessionEntity?

    @Query("SELECT * FROM planned_session WHERE mesoId = :mesoId AND week = :week ORDER BY dayIndex")
    suspend fun getWeek(
        mesoId: Long,
        week: Int,
    ): List<PlannedSessionEntity>

    @Query("SELECT * FROM planned_session WHERE mesoId = :mesoId ORDER BY week, dayIndex")
    suspend fun getAllForMeso(mesoId: Long): List<PlannedSessionEntity>

    @Query("UPDATE planned_session SET state = 'IN_PROGRESS' WHERE id = :id AND state = 'PENDING'")
    suspend fun markStarted(id: Long)

    @Query(
        "UPDATE planned_session SET state = 'COMPLETED', completedAtEpochMillis = :completedAtEpochMillis " +
            "WHERE id = :id",
    )
    suspend fun markCompleted(
        id: Long,
        completedAtEpochMillis: Long,
    )

    /** Early-deload skip (DOMAIN.md §6): remaining accumulation days are dropped, deload days stay. */
    @Query("UPDATE planned_session SET state = 'SKIPPED' WHERE mesoId = :mesoId AND isDeload = 0 AND state = 'PENDING'")
    suspend fun skipPendingAccumulation(mesoId: Long)

    @Query(
        "SELECT COUNT(*) FROM planned_session WHERE mesoId = :mesoId " +
            "AND (week > :week OR (week = :week AND dayIndex > :dayIndex))",
    )
    suspend fun countAfter(
        mesoId: Long,
        week: Int,
        dayIndex: Int,
    ): Int
}

/** Per-session per-muscle set prescriptions. */
@Dao
interface SessionMuscleTargetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(rows: List<SessionMuscleTargetEntity>)

    @Query("SELECT * FROM session_muscle_target WHERE sessionId = :sessionId")
    fun observeFor(sessionId: Long): Flow<List<SessionMuscleTargetEntity>>

    @Query("SELECT * FROM session_muscle_target WHERE sessionId = :sessionId")
    suspend fun getFor(sessionId: Long): List<SessionMuscleTargetEntity>

    @Query("DELETE FROM session_muscle_target WHERE sessionId = :sessionId AND muscle = :muscle")
    suspend fun delete(
        sessionId: Long,
        muscle: Muscle,
    )
}

/** Live per-muscle weekly state for a running mesocycle. */
@Dao
interface MuscleWeekStateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(rows: List<MuscleWeekStateEntity>)

    @Query("SELECT * FROM muscle_week_state WHERE mesoId = :mesoId")
    suspend fun getFor(mesoId: Long): List<MuscleWeekStateEntity>
}

/** Logged working sets. */
@Dao
interface LoggedSetDao {
    @Insert
    suspend fun insert(row: LoggedSetEntity): Long

    @Query("DELETE FROM logged_set WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM logged_set WHERE sessionId = :sessionId ORDER BY orderInSession, id")
    fun observeFor(sessionId: Long): Flow<List<LoggedSetEntity>>
}

/** Per-session per-muscle feedback rows. */
@Dao
interface MuscleFeedbackDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(rows: List<MuscleFeedbackEntity>)

    @Query("SELECT * FROM muscle_feedback WHERE sessionId = :sessionId")
    suspend fun getFor(sessionId: Long): List<MuscleFeedbackEntity>
}

/** Persisted engine decisions (the UI's "why" history). */
@Dao
interface DecisionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(rows: List<DecisionEntity>)

    @Query("SELECT * FROM decision WHERE sessionId = :sessionId")
    suspend fun getFor(sessionId: Long): List<DecisionEntity>
}
