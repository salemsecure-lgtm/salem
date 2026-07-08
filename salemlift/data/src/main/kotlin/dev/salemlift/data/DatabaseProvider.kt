package dev.salemlift.data

import android.content.Context
import androidx.room.Room
import androidx.room.withTransaction
import dev.salemlift.data.analytics.AnalyticsRepository
import dev.salemlift.data.analytics.DefaultAnalyticsRepository
import dev.salemlift.data.catalog.AssetSource
import dev.salemlift.data.catalog.ExerciseSeeder
import dev.salemlift.data.catalog.SeedResult
import dev.salemlift.data.catalog.TransactionRunner
import dev.salemlift.data.db.SalemDatabase
import dev.salemlift.data.settings.DefaultSettingsRepository
import dev.salemlift.data.settings.SettingsRepository

/**
 * Process-wide lazy singleton wiring for [SalemDatabase] and its consumers.
 * This is the only place production code reads the wall clock — everything
 * downstream takes an injected `() -> Long`.
 */
object DatabaseProvider {
    @Volatile
    private var instance: SalemDatabase? = null

    fun database(context: Context): SalemDatabase =
        instance ?: synchronized(this) {
            instance ?: buildDatabase(context).also { instance = it }
        }

    /**
     * Read-only exercise catalog access for the app layer's picker. Exposed
     * here because Room is implementation-scoped in this module, so :app
     * cannot call [SalemDatabase.exerciseDao] through the RoomDatabase
     * supertype it can't see.
     */
    fun exerciseDao(context: Context): dev.salemlift.data.db.ExerciseDao = database(context).exerciseDao()

    fun trainingRepository(context: Context): TrainingRepository =
        DefaultTrainingRepository(
            database = database(context),
            nowEpochMillis = System::currentTimeMillis,
        )

    fun analyticsRepository(context: Context): AnalyticsRepository = DefaultAnalyticsRepository(database(context))

    /** Settings persistence (landmarks, rule deltas, rest pref) + backup/export. */
    fun settingsRepository(context: Context): SettingsRepository = DefaultSettingsRepository(database(context))

    /** First-launch exercise-catalog seeding (idempotent); the :app layer calls this at startup. */
    suspend fun ensureSeeded(context: Context): SeedResult {
        val database = database(context)
        val seeder =
            ExerciseSeeder(
                assets = AssetSource { context.assets.open(it) },
                dao = database.exerciseDao(),
                transactionRunner = TransactionRunner { block -> database.withTransaction { block() } },
            )
        return seeder.seedIfEmpty()
    }

    private fun buildDatabase(context: Context): SalemDatabase =
        Room
            .databaseBuilder(context.applicationContext, SalemDatabase::class.java, SalemDatabase.NAME)
            .build()
}
