package dev.salemlift.domain.engine

import dev.salemlift.domain.model.DefaultLandmarks
import dev.salemlift.domain.model.Landmarks
import dev.salemlift.domain.model.MesoConfig
import dev.salemlift.domain.model.Muscle
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** DOMAIN.md §3 mesocycle planning. */
class MesocyclePlannerTest {
    @Test
    fun `default four week meso has five weeks ending in a deload`() {
        val plan = MesocyclePlanner.plan(MesoConfig(), DefaultLandmarks.seeds)
        assertEquals(5, plan.weeks.size)
        assertTrue(plan.weeks.last().isDeload)
        assertTrue(plan.weeks.dropLast(1).none { it.isDeload })
        assertEquals(plan.weeks.last(), plan.deloadWeek)
        assertEquals((1..5).toList(), plan.weeks.map { it.week })
    }

    @Test
    fun `every muscle starts week one at its MEV`() {
        val plan = MesocyclePlanner.plan(MesoConfig(), DefaultLandmarks.seeds)
        for ((muscle, landmarks) in DefaultLandmarks.seeds) {
            assertEquals(
                landmarks.mev,
                plan.weeks.first().setTargets.getValue(muscle),
                "week 1 target for $muscle",
            )
        }
    }

    @Test
    fun `projected accumulation climbs plus one per week clamped to MRV`() {
        val tight = mapOf(Muscle.FOREARMS to Landmarks(mev = 4, mrv = 6))
        val plan = MesocyclePlanner.plan(MesoConfig(), tight)
        val targets = plan.weeks.dropLast(1).map { it.setTargets.getValue(Muscle.FOREARMS) }
        assertEquals(listOf(4, 5, 6, 6), targets)
    }

    @Test
    fun `deload week prescribes MV volume and reduced load`() {
        val config = MesoConfig(deloadLoadMultiplier = 0.85)
        val plan = MesocyclePlanner.plan(config, DefaultLandmarks.seeds)
        val deload = plan.deloadWeek
        for ((muscle, landmarks) in DefaultLandmarks.seeds) {
            assertEquals(landmarks.mv, deload.setTargets.getValue(muscle), "deload target for $muscle")
        }
        assertEquals(0.85, deload.loadMultiplier)
        assertEquals(RirSchedule.deload, deload.effort)
        assertTrue(plan.weeks.dropLast(1).all { it.loadMultiplier == 1.0 })
    }

    @Test
    fun `accumulation efforts follow the RIR schedule`() {
        val plan = MesocyclePlanner.plan(MesoConfig(accumulationWeeks = 6), DefaultLandmarks.seeds)
        assertEquals(RirSchedule.forAccumulation(6), plan.weeks.dropLast(1).map { it.effort })
        assertEquals(7, plan.weeks.size)
        assertFalse(plan.weeks[5].isDeload)
        assertTrue(plan.weeks[6].isDeload)
    }

    @Test
    fun `planning requires at least one muscle`() {
        assertThrows<IllegalArgumentException> {
            MesocyclePlanner.plan(MesoConfig(), emptyMap())
        }
    }

    @Test
    fun `config rejects out-of-range values`() {
        assertThrows<IllegalArgumentException> { MesoConfig(accumulationWeeks = 3) }
        assertThrows<IllegalArgumentException> { MesoConfig(accumulationWeeks = 7) }
        assertThrows<IllegalArgumentException> { MesoConfig(deloadLoadMultiplier = 0.5) }
        assertThrows<IllegalArgumentException> { MesoConfig(deloadLoadMultiplier = 0.95) }
    }
}
