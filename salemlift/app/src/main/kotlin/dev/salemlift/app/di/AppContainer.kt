package dev.salemlift.app.di

import android.content.Context
import dev.salemlift.data.DatabaseProvider
import dev.salemlift.data.TrainingRepository
import dev.salemlift.data.analytics.AnalyticsRepository
import dev.salemlift.data.db.ExerciseDao
import dev.salemlift.data.settings.SettingsRepository

/**
 * Manual dependency wiring for the app layer. Everything is lazy so nothing
 * touches Room until a screen actually needs it.
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    val repository: TrainingRepository by lazy { DatabaseProvider.trainingRepository(appContext) }

    /** Read-only aggregations for the analytics screens. */
    val analyticsRepository: AnalyticsRepository by lazy { DatabaseProvider.analyticsRepository(appContext) }

    /** Settings persistence (landmarks, rule deltas, rest pref) + backup/export. */
    val settingsRepository: SettingsRepository by lazy { DatabaseProvider.settingsRepository(appContext) }

    /** Read-only exercise queries for the picker (the one allowed direct-DAO surface). */
    val exerciseDao: ExerciseDao by lazy { DatabaseProvider.exerciseDao(appContext) }

    /** Resolves a logged set's exerciseId back to a display name after process death. */
    val exerciseNameResolver: ExerciseNameResolver by lazy {
        ExerciseNameResolver { id -> exerciseDao.getById(id)?.name }
    }

    /** Hands the freshly committed [dev.salemlift.data.CommitOutcome] to the summary screen. */
    val commitResultStore: CommitResultStore = CommitResultStore()

    /** First-launch exercise-catalog seeding; idempotent. */
    suspend fun ensureSeeded() {
        DatabaseProvider.ensureSeeded(appContext)
    }
}

/** Thin read-only lookup so ViewModels never hold a DAO directly. */
fun interface ExerciseNameResolver {
    suspend fun name(exerciseId: String): String?
}
