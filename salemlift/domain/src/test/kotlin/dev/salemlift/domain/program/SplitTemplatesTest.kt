package dev.salemlift.domain.program

import dev.salemlift.domain.model.Muscle
import dev.salemlift.domain.model.SessionTemplate
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SplitTemplatesTest {
    @Test
    fun `every shipped template trains all fourteen muscles`() {
        for (split in SplitTemplates.all) {
            assertEquals(
                Muscle.entries.toSet(),
                split.trainedMuscles,
                "${split.name} must not silently drop a muscle",
            )
        }
    }

    @Test
    fun `shipped templates have the expected session counts`() {
        assertEquals(3, SplitTemplates.pplThreeDay.sessions.size)
        assertEquals(6, SplitTemplates.pplSixDay.sessions.size)
        assertEquals(4, SplitTemplates.upperLowerFourDay.sessions.size)
        assertEquals(3, SplitTemplates.fullBodyThreeDay.sessions.size)
    }

    @Test
    fun `session muscles are distinct within each session`() {
        for (split in SplitTemplates.all) {
            for (session in split.sessions) {
                assertEquals(session.muscles.size, session.muscles.toSet().size, session.name)
            }
        }
    }

    @Test
    fun `custom splits are validated by the model`() {
        val custom =
            SplitTemplates.custom(
                "Arms only",
                listOf(SessionTemplate("Arms", listOf(Muscle.BICEPS, Muscle.TRICEPS))),
            )
        assertTrue(custom.trainedMuscles == setOf(Muscle.BICEPS, Muscle.TRICEPS))
        assertThrows<IllegalArgumentException> { SplitTemplates.custom("", emptyList()) }
        assertThrows<IllegalArgumentException> {
            SplitTemplates.custom("Dup", listOf(SessionTemplate("A", listOf(Muscle.CHEST, Muscle.CHEST))))
        }
        assertThrows<IllegalArgumentException> {
            SplitTemplates.custom("Empty day", listOf(SessionTemplate("A", emptyList())))
        }
        assertThrows<IllegalArgumentException> {
            SplitTemplates.custom("Blank day", listOf(SessionTemplate(" ", listOf(Muscle.CHEST))))
        }
    }
}
