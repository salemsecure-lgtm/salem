package dev.salemlift.data.catalog

import dev.salemlift.domain.model.Muscle
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/** Raw JSON shape of one free-exercise-db entry. Unknown keys (e.g. `images`) are ignored. */
@Serializable
data class ExerciseDto(
    val id: String,
    val name: String,
    val primaryMuscles: List<String> = emptyList(),
    val secondaryMuscles: List<String> = emptyList(),
    val equipment: String? = null,
    val category: String = "",
    val level: String = "",
    val mechanic: String? = null,
    val force: String? = null,
    val instructions: List<String> = emptyList(),
)

/** Normalized equipment tags for the free-exercise-db equipment vocabulary. */
enum class Equipment {
    BANDS,
    BARBELL,
    BODY_ONLY,
    CABLE,
    DUMBBELL,
    EZ_CURL_BAR,
    EXERCISE_BALL,
    FOAM_ROLL,
    KETTLEBELLS,
    MACHINE,
    MEDICINE_BALL,
    OTHER,
    ;

    companion object {
        /** Maps a raw dataset equipment string to a tag; `null`/unknown values become [OTHER]. */
        fun fromDataset(raw: String?): Equipment =
            when (raw?.trim()?.lowercase()) {
                "bands" -> BANDS
                "barbell" -> BARBELL
                "body only" -> BODY_ONLY
                "cable" -> CABLE
                "dumbbell" -> DUMBBELL
                "e-z curl bar" -> EZ_CURL_BAR
                "exercise ball" -> EXERCISE_BALL
                "foam roll" -> FOAM_ROLL
                "kettlebells" -> KETTLEBELLS
                "machine" -> MACHINE
                "medicine ball" -> MEDICINE_BALL
                else -> OTHER
            }
    }
}

/**
 * Maps free-exercise-db muscle strings onto the 14 canonical [Muscle] groups (DOMAIN.md §1.1).
 *
 * Direct mappings: abdominals→ABS, biceps→BICEPS, calves→CALVES, chest→CHEST, forearms→FOREARMS,
 * glutes→GLUTES, hamstrings→HAMSTRINGS, quadriceps→QUADS, triceps→TRICEPS, traps→TRAPS,
 * lats/"middle back"/"lower back"→BACK, neck→TRAPS, abductors→GLUTES, adductors→QUADS.
 *
 * ## Shoulder refinement policy
 * The dataset collapses all three delt heads into a single "shoulders" string, so the head is
 * inferred from the exercise name (case-insensitive substring checks), in this precedence order:
 * 1. Rear-delt keywords ("rear", "reverse fl", "face pull") → [Muscle.REAR_DELTS]. Checked first so
 *    names such as "Dumbbell Lying Rear Lateral Raise" resolve to rear delts, not side delts.
 * 2. Side-delt keywords ("lateral raise", "side raise", "side lateral") → [Muscle.SIDE_DELTS].
 * 3. Front-raise pattern (name contains both "front" and "raise", covering "Front Raise" as well as
 *    "Front Dumbbell Raise" / "Front Plate Raise" phrasings) → [Muscle.FRONT_DELTS].
 * 4. Pressing keywords ("press" — which subsumes "push press" — and "jerk") → [Muscle.FRONT_DELTS].
 * 5. Anything else defaults to [Muscle.SIDE_DELTS].
 */
object MuscleMapper {
    private const val SHOULDERS = "shoulders"

    private val direct: Map<String, Muscle> =
        mapOf(
            "abdominals" to Muscle.ABS,
            "abductors" to Muscle.GLUTES,
            "adductors" to Muscle.QUADS,
            "biceps" to Muscle.BICEPS,
            "calves" to Muscle.CALVES,
            "chest" to Muscle.CHEST,
            "forearms" to Muscle.FOREARMS,
            "glutes" to Muscle.GLUTES,
            "hamstrings" to Muscle.HAMSTRINGS,
            "lats" to Muscle.BACK,
            "lower back" to Muscle.BACK,
            "middle back" to Muscle.BACK,
            "neck" to Muscle.TRAPS,
            "quadriceps" to Muscle.QUADS,
            "traps" to Muscle.TRAPS,
            "triceps" to Muscle.TRICEPS,
        )

    private val rearDeltKeywords = listOf("rear", "reverse fl", "face pull")
    private val sideDeltKeywords = listOf("lateral raise", "side raise", "side lateral")
    private val frontDeltPressKeywords = listOf("press", "jerk")

    /**
     * Maps a dataset muscle string to a canonical [Muscle], or `null` if the string is outside the
     * known vocabulary. [exerciseName] is used only to refine the ambiguous "shoulders" value.
     */
    fun map(
        datasetMuscle: String,
        exerciseName: String,
    ): Muscle? {
        val key = datasetMuscle.trim().lowercase()
        return if (key == SHOULDERS) refineShoulders(exerciseName) else direct[key]
    }

    /** Resolves "shoulders" to a delt head from the exercise name; see the class kdoc for the policy. */
    fun refineShoulders(exerciseName: String): Muscle {
        val name = exerciseName.lowercase()
        return when {
            rearDeltKeywords.any { it in name } -> Muscle.REAR_DELTS
            sideDeltKeywords.any { it in name } -> Muscle.SIDE_DELTS
            "front" in name && "raise" in name -> Muscle.FRONT_DELTS
            frontDeltPressKeywords.any { it in name } -> Muscle.FRONT_DELTS
            else -> Muscle.SIDE_DELTS
        }
    }
}

/** A catalog exercise mapped onto the canonical domain vocabulary, ready for seeding. */
data class CatalogExercise(
    val id: String,
    val name: String,
    val primaryMuscle: Muscle,
    val secondaryMuscles: Set<Muscle>,
    val equipment: Equipment,
    val level: String,
    val category: String,
    val cues: List<String>,
    val isCustom: Boolean = false,
)

/** One dataset entry excluded from seeding, with the reason it was dropped. */
data class ExcludedExercise(
    val id: String,
    val name: String,
    val reason: String,
)

/** Outcome of a parse/seed pass: `seeded + excluded.size == total` always holds. */
data class SeedReport(
    val total: Int,
    val seeded: Int,
    val excluded: List<ExcludedExercise>,
)

/** Parsed catalog plus its [SeedReport]. */
data class CatalogParseResult(
    val exercises: List<CatalogExercise>,
    val report: SeedReport,
)

/**
 * Pure-Kotlin parser for the bundled free-exercise-db dataset (`assets/exercises.json`, public
 * domain — see `assets/exercises-LICENSE.md`). This file must stay free of Android imports so the
 * parsing and mapping logic is testable as a plain JVM unit test against the real bundled JSON.
 *
 * Entries whose primary muscle list is empty, or whose first primary muscle cannot be mapped to a
 * canonical [Muscle], are excluded and reported — a broken muscle mapping would silently corrupt
 * the volume engine's set accounting, so exclusion is the safe failure mode.
 */
class ExerciseCatalogParser {
    private val json = Json { ignoreUnknownKeys = true }
    private val listSerializer = ListSerializer(ExerciseDto.serializer())

    /** Parses [jsonText] (the full `exercises.json` payload) into a [CatalogParseResult]. */
    fun parse(jsonText: String): CatalogParseResult {
        val dtos = json.decodeFromString(listSerializer, jsonText)
        val seeded = ArrayList<CatalogExercise>(dtos.size)
        val excluded = mutableListOf<ExcludedExercise>()
        for (dto in dtos) {
            val exclusionReason = exclusionReasonFor(dto)
            if (exclusionReason != null) {
                excluded += ExcludedExercise(id = dto.id, name = dto.name, reason = exclusionReason)
            } else {
                seeded += toCatalogExercise(dto)
            }
        }
        val report = SeedReport(total = dtos.size, seeded = seeded.size, excluded = excluded)
        return CatalogParseResult(exercises = seeded, report = report)
    }

    private fun exclusionReasonFor(dto: ExerciseDto): String? {
        val rawPrimary = dto.primaryMuscles.firstOrNull()
        return when {
            dto.id.isBlank() || dto.name.isBlank() -> "blank id or name"
            rawPrimary == null -> "no primary muscle listed"
            MuscleMapper.map(rawPrimary, dto.name) == null -> "unmappable primary muscle \"$rawPrimary\""
            else -> null
        }
    }

    private fun toCatalogExercise(dto: ExerciseDto): CatalogExercise {
        val rawPrimary = dto.primaryMuscles.first()
        val primary =
            requireNotNull(MuscleMapper.map(rawPrimary, dto.name)) {
                "toCatalogExercise called for unmappable entry ${dto.id}"
            }
        val secondaries =
            dto.secondaryMuscles
                .mapNotNull { MuscleMapper.map(it, dto.name) }
                .toSet() - primary
        return CatalogExercise(
            id = dto.id,
            name = dto.name,
            primaryMuscle = primary,
            secondaryMuscles = secondaries,
            equipment = Equipment.fromDataset(dto.equipment),
            level = dto.level,
            category = dto.category,
            cues = dto.instructions,
            isCustom = false,
        )
    }
}
