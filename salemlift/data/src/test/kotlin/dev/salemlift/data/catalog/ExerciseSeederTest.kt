package dev.salemlift.data.catalog

import dev.salemlift.data.db.ExerciseDao
import dev.salemlift.data.db.ExerciseEntity
import dev.salemlift.domain.model.Muscle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.InputStream
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/** Pure-JVM seeder tests using a fake DAO and fake asset source (no Robolectric, no Android). */
class ExerciseSeederTest {
    private val runner = RecordingTransactionRunner()
    private val dao = FakeExerciseDao(runner)
    private val assets = CountingAssetSource(SAMPLE_JSON)
    private val seeder = ExerciseSeeder(assets = assets, dao = dao, transactionRunner = runner)

    @Test
    fun `seeds an empty database inside a single transaction`() =
        runBlocking {
            val result = seeder.seedIfEmpty()

            val seeded = assertIs<SeedResult.Seeded>(result)
            assertEquals(2, seeded.report.total)
            assertEquals(2, seeded.report.seeded)
            assertTrue(seeded.report.excluded.isEmpty())
            assertEquals(2, dao.countAll())
            assertEquals(1, runner.invocations, "expected exactly one transaction")
            assertEquals(1, dao.insertAllCalls, "expected one bulk insert")
            assertTrue(dao.allInsertsInsideTransaction, "insertAll ran outside the transaction")
            assertEquals(Muscle.CHEST, dao.rows.getValue("bench").primaryMuscle)
            assertEquals(setOf(Muscle.TRICEPS, Muscle.FRONT_DELTS), dao.rows.getValue("bench").secondaryMuscles)
        }

    @Test
    fun `second run is a no-op AlreadySeeded`() =
        runBlocking {
            seeder.seedIfEmpty()
            val second = seeder.seedIfEmpty()

            val skipped = assertIs<SeedResult.AlreadySeeded>(second)
            assertEquals(2, skipped.existingCount)
            assertEquals(1, assets.opens, "asset must not be re-read on a no-op run")
            assertEquals(1, runner.invocations)
            assertEquals(1, dao.insertAllCalls)
            assertEquals(2, dao.countAll())
        }

    @Test
    fun `pre-populated database is never re-seeded and the asset is never opened`() =
        runBlocking {
            dao.rows["existing"] = entity("existing")

            val result = seeder.seedIfEmpty()

            val skipped = assertIs<SeedResult.AlreadySeeded>(result)
            assertEquals(1, skipped.existingCount)
            assertEquals(0, assets.opens)
            assertEquals(0, runner.invocations)
            assertEquals(0, dao.insertAllCalls)
        }

    @Test
    fun `unmappable entries are excluded and reported, mappable ones still seed`() =
        runBlocking {
            val mixedAssets = CountingAssetSource(MIXED_JSON)
            val mixedSeeder = ExerciseSeeder(assets = mixedAssets, dao = dao, transactionRunner = runner)

            val result = mixedSeeder.seedIfEmpty()

            val seeded = assertIs<SeedResult.Seeded>(result)
            assertEquals(2, seeded.report.total)
            assertEquals(1, seeded.report.seeded)
            assertEquals(1, seeded.report.excluded.size)
            assertEquals("mystery", seeded.report.excluded.single().id)
            assertTrue(seeded.report.excluded.single().reason.isNotBlank())
            assertEquals(1, dao.countAll())
        }

    private fun entity(id: String): ExerciseEntity =
        ExerciseEntity(
            id = id,
            name = id,
            primaryMuscle = Muscle.CHEST,
            secondaryMuscles = emptySet(),
            equipment = Equipment.OTHER,
            level = "beginner",
            category = "strength",
            cues = emptyList(),
            isCustom = false,
        )

    private class CountingAssetSource(private val payload: String) : AssetSource {
        var opens = 0

        override fun open(name: String): InputStream {
            opens++
            return ByteArrayInputStream(payload.encodeToByteArray())
        }
    }

    private class RecordingTransactionRunner : TransactionRunner {
        var invocations = 0
        var active = false

        override suspend fun runInTransaction(block: suspend () -> Unit) {
            invocations++
            active = true
            try {
                block()
            } finally {
                active = false
            }
        }
    }

    private class FakeExerciseDao(private val runner: RecordingTransactionRunner) : ExerciseDao {
        val rows = linkedMapOf<String, ExerciseEntity>()
        var insertAllCalls = 0
        var allInsertsInsideTransaction = true

        override fun getAll(): Flow<List<ExerciseEntity>> = flowOf(rows.values.toList())

        override suspend fun getById(id: String): ExerciseEntity? = rows[id]

        override fun searchByNameEscaped(escaped: String): Flow<List<ExerciseEntity>> =
            flowOf(rows.values.filter { it.name.contains(escaped, ignoreCase = true) })

        override fun filterByPrimaryMuscle(muscle: Muscle): Flow<List<ExerciseEntity>> =
            flowOf(rows.values.filter { it.primaryMuscle == muscle })

        override fun filterByEquipment(equipment: Equipment): Flow<List<ExerciseEntity>> =
            flowOf(rows.values.filter { it.equipment == equipment })

        override suspend fun insertAll(exercises: List<ExerciseEntity>) {
            insertAllCalls++
            allInsertsInsideTransaction = allInsertsInsideTransaction && runner.active
            exercises.forEach { rows[it.id] = it }
        }

        override suspend fun insert(exercise: ExerciseEntity) {
            check(exercise.id !in rows) { "id collision: ${exercise.id}" }
            rows[exercise.id] = exercise
        }

        override suspend fun countAll(): Int = rows.size

        override suspend fun getCustom(): List<ExerciseEntity> = rows.values.filter { it.isCustom }

        override suspend fun deleteCustom() {
            rows.values.removeAll { it.isCustom }
        }
    }

    private companion object {
        val SAMPLE_JSON =
            """
            [
              {"id":"bench","name":"Bench Press","primaryMuscles":["chest"],
               "secondaryMuscles":["triceps","shoulders"],"equipment":"barbell",
               "category":"strength","level":"beginner","instructions":["Press the bar."]},
              {"id":"curl","name":"Barbell Curl","primaryMuscles":["biceps"],
               "secondaryMuscles":["forearms"],"equipment":"barbell",
               "category":"strength","level":"beginner","instructions":["Curl the bar."]}
            ]
            """.trimIndent()

        val MIXED_JSON =
            """
            [
              {"id":"mystery","name":"Mystery Move","primaryMuscles":[],
               "secondaryMuscles":[],"equipment":null,
               "category":"strength","level":"beginner","instructions":[]},
              {"id":"squat","name":"Squat","primaryMuscles":["quadriceps"],
               "secondaryMuscles":["glutes","hamstrings"],"equipment":"barbell",
               "category":"strength","level":"beginner","instructions":["Squat down."]}
            ]
            """.trimIndent()
    }
}
