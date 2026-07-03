package dev.salemlift.data.catalog

import dev.salemlift.domain.model.Muscle
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Runs the parser against the real bundled `exercises.json` (873 free-exercise-db entries). */
class ExerciseCatalogParserTest {
    @Test
    fun `parses all 873 entries and the report reconciles`() {
        val report = result.report
        assertEquals(DATASET_SIZE, report.total)
        assertEquals(report.total, report.seeded + report.excluded.size)
        assertEquals(report.seeded, result.exercises.size)
        assertTrue(report.excluded.size < MAX_EXCLUSIONS, "too many exclusions: ${report.excluded}")
        report.excluded.forEach { exclusion ->
            assertTrue(exclusion.id.isNotBlank(), "exclusion without id: $exclusion")
            assertTrue(exclusion.reason.isNotBlank(), "exclusion without reason: $exclusion")
        }
    }

    @Test
    fun `every seeded exercise has a canonical primary muscle, non-blank identity, and preserved cues`() {
        val instructionsById = rawDtos.associateBy({ it.id }, { it.instructions })
        assertTrue(result.exercises.isNotEmpty())
        result.exercises.forEach { exercise ->
            assertTrue(exercise.id.isNotBlank(), "blank id: $exercise")
            assertTrue(exercise.name.isNotBlank(), "blank name for ${exercise.id}")
            assertTrue(exercise.primaryMuscle in Muscle.entries, "bad primary for ${exercise.id}")
            assertEquals(instructionsById.getValue(exercise.id), exercise.cues, "cues altered for ${exercise.id}")
            assertTrue(exercise.primaryMuscle !in exercise.secondaryMuscles, "primary duplicated for ${exercise.id}")
            assertTrue(!exercise.isCustom, "seeded exercise flagged custom: ${exercise.id}")
        }
    }

    @Test
    fun `all 14 canonical muscles are covered by at least one primary mapping`() {
        val histogram =
            result.exercises
                .groupingBy { it.primaryMuscle }
                .eachCount()
        println("Primary-muscle histogram (seeded=${result.report.seeded}):")
        Muscle.entries.forEach { muscle ->
            println("  %-12s %d".format(muscle.name, histogram[muscle] ?: 0))
        }
        Muscle.entries.forEach { muscle ->
            assertTrue((histogram[muscle] ?: 0) > 0, "no seeded exercise credits $muscle as primary")
        }
    }

    @Test
    fun `shoulder refinement spot checks on pinned real dataset entries`() {
        val byId = result.exercises.associateBy { it.id }
        assertEquals(Muscle.SIDE_DELTS, byId.getValue("Side_Lateral_Raise").primaryMuscle)
        assertEquals(Muscle.FRONT_DELTS, byId.getValue("Barbell_Shoulder_Press").primaryMuscle)
        assertEquals(Muscle.REAR_DELTS, byId.getValue("Reverse_Flyes").primaryMuscle)
    }

    private companion object {
        const val DATASET_SIZE = 873
        const val MAX_EXCLUSIONS = 30

        val datasetFile = File("src/main/assets/exercises.json")

        val result: CatalogParseResult by lazy {
            ExerciseCatalogParser().parse(datasetFile.readText())
        }

        val rawDtos: List<ExerciseDto> by lazy {
            val json = Json { ignoreUnknownKeys = true }
            json.decodeFromString(ListSerializer(ExerciseDto.serializer()), datasetFile.readText())
        }
    }
}
