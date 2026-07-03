package dev.salemlift.data.catalog

import dev.salemlift.domain.model.Muscle
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class MuscleMapperTest {
    @Test
    fun `every dataset vocabulary string maps to a canonical muscle`() {
        val vocabulary =
            listOf(
                "abdominals", "abductors", "adductors", "biceps", "calves", "chest", "forearms",
                "glutes", "hamstrings", "lats", "lower back", "middle back", "neck", "quadriceps",
                "shoulders", "traps", "triceps",
            )
        vocabulary.forEach { raw ->
            assertNotNull(MuscleMapper.map(raw, "Any Exercise"), "\"$raw\" failed to map")
        }
    }

    @Test
    fun `direct mappings follow the documented table`() {
        val expected =
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
        expected.forEach { (raw, muscle) ->
            assertEquals(muscle, MuscleMapper.map(raw, "Any Exercise"), "wrong mapping for \"$raw\"")
        }
    }

    @Test
    fun `unknown muscle strings map to null`() {
        assertNull(MuscleMapper.map("obliques", "Side Bend"))
        assertNull(MuscleMapper.map("", "Side Bend"))
    }

    @Test
    fun `shoulders refinement resolves the delt head from the exercise name`() {
        assertEquals(Muscle.SIDE_DELTS, MuscleMapper.map("shoulders", "Side Lateral Raise"))
        assertEquals(Muscle.SIDE_DELTS, MuscleMapper.map("shoulders", "Bent Over Low-Pulley Side Lateral"))
        assertEquals(Muscle.REAR_DELTS, MuscleMapper.map("shoulders", "Reverse Flyes"))
        assertEquals(Muscle.REAR_DELTS, MuscleMapper.map("shoulders", "Face Pull"))
        assertEquals(Muscle.FRONT_DELTS, MuscleMapper.map("shoulders", "Front Dumbbell Raise"))
        assertEquals(Muscle.FRONT_DELTS, MuscleMapper.map("shoulders", "Barbell Shoulder Press"))
        assertEquals(Muscle.FRONT_DELTS, MuscleMapper.map("shoulders", "Double Kettlebell Push Press"))
        assertEquals(Muscle.FRONT_DELTS, MuscleMapper.map("shoulders", "Clean and Jerk"))
    }

    @Test
    fun `rear-delt keywords take precedence over side-delt keywords`() {
        assertEquals(Muscle.REAR_DELTS, MuscleMapper.map("shoulders", "Dumbbell Lying Rear Lateral Raise"))
    }

    @Test
    fun `unrecognized shoulder exercises default to side delts`() {
        assertEquals(Muscle.SIDE_DELTS, MuscleMapper.map("shoulders", "Arm Circles"))
        assertEquals(Muscle.SIDE_DELTS, MuscleMapper.map("shoulders", "Battling Ropes"))
    }
}
