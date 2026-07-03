package dev.salemlift.data.catalog

import dev.salemlift.data.db.ExerciseDao
import java.io.InputStream

/**
 * Abstraction over Android's `AssetManager` so seeding logic stays JVM-testable.
 * Production wiring: `AssetSource { name -> context.assets.open(name) }`.
 */
fun interface AssetSource {
    fun open(name: String): InputStream
}

/**
 * Runs [block] inside a single database transaction.
 * Production wiring: `TransactionRunner { block -> database.withTransaction { block() } }`.
 */
fun interface TransactionRunner {
    suspend fun runInTransaction(block: suspend () -> Unit)
}

/** Result of a [ExerciseSeeder.seedIfEmpty] call. */
sealed interface SeedResult {
    /** The catalog was empty and has been seeded; [report] describes what was inserted/excluded. */
    data class Seeded(val report: SeedReport) : SeedResult

    /** The catalog already holds [existingCount] rows; nothing was parsed or inserted. */
    data class AlreadySeeded(val existingCount: Int) : SeedResult
}

/**
 * First-launch seeder for the bundled free-exercise-db catalog.
 *
 * - **Idempotent:** seeding is skipped when the exercises table is non-empty, so app restarts and
 *   process deaths never duplicate rows.
 * - **Transactional:** all rows are inserted through a single [TransactionRunner] invocation, so a
 *   crash mid-seed leaves the table empty and the next launch retries cleanly.
 * - **Versioned:** [CATALOG_VERSION] tracks the bundled dataset revision. A future dataset update
 *   bumps it together with a Room migration that re-seeds non-custom rows (custom exercises are
 *   preserved via [CatalogExercise.isCustom]).
 */
class ExerciseSeeder(
    private val assets: AssetSource,
    private val dao: ExerciseDao,
    private val transactionRunner: TransactionRunner,
    private val parser: ExerciseCatalogParser = ExerciseCatalogParser(),
) {
    /**
     * Seeds the catalog from [assetName] unless the table already has rows.
     * Returns the [SeedReport] on a fresh seed, or [SeedResult.AlreadySeeded] on a no-op.
     */
    suspend fun seedIfEmpty(assetName: String = ASSET_NAME): SeedResult {
        val existing = dao.countAll()
        if (existing > 0) {
            return SeedResult.AlreadySeeded(existingCount = existing)
        }
        val jsonText = assets.open(assetName).use { it.readBytes().decodeToString() }
        val parsed = parser.parse(jsonText)
        transactionRunner.runInTransaction {
            dao.insertAll(parsed.exercises.map { it.toEntity() })
        }
        return SeedResult.Seeded(report = parsed.report)
    }

    companion object {
        /** Asset file name of the bundled dataset. */
        const val ASSET_NAME: String = "exercises.json"

        /** Revision of the bundled free-exercise-db snapshot; bump on dataset updates. */
        const val CATALOG_VERSION: Int = 1
    }
}
