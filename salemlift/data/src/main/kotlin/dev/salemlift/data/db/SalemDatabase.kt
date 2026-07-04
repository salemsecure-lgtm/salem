package dev.salemlift.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * Salem Lift's Room database. Version 1 holds the exercise catalog plus the Phase 3
 * tracker tables (mesocycles, planned sessions, targets, logged sets, feedback,
 * decisions, landmarks, weekly state). The app has never shipped, so the tracker
 * tables were folded into version 1 rather than added as a migration; the exported
 * schema under `data/schemas/` was regenerated accordingly.
 */
@Database(
    entities = [
        ExerciseEntity::class,
        LandmarkEntity::class,
        MesocycleEntity::class,
        PlannedSessionEntity::class,
        SessionMuscleTargetEntity::class,
        MuscleWeekStateEntity::class,
        LoggedSetEntity::class,
        MuscleFeedbackEntity::class,
        DecisionEntity::class,
    ],
    version = SalemDatabase.VERSION,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class SalemDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao

    abstract fun landmarkDao(): LandmarkDao

    abstract fun mesocycleDao(): MesocycleDao

    abstract fun plannedSessionDao(): PlannedSessionDao

    abstract fun sessionMuscleTargetDao(): SessionMuscleTargetDao

    abstract fun muscleWeekStateDao(): MuscleWeekStateDao

    abstract fun loggedSetDao(): LoggedSetDao

    abstract fun muscleFeedbackDao(): MuscleFeedbackDao

    abstract fun decisionDao(): DecisionDao

    /** Read-only analytics aggregations over the tables above (no schema impact). */
    abstract fun analyticsDao(): AnalyticsDao

    companion object {
        const val VERSION: Int = 1
        const val NAME: String = "salemlift.db"
    }
}
