package dev.salemlift.domain.model

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** DOMAIN.md §2 landmark derivation, seeds, and experience scaling. */
class LandmarksTest {
    @Test
    fun `MV defaults to round half of MEV with floor 2`() {
        assertEquals(4, Landmarks(mev = 8, mrv = 22).mv)
        assertEquals(5, Landmarks(mev = 9, mrv = 22).mv)
        assertEquals(2, Landmarks(mev = 2, mrv = 10).mv)
        assertEquals(2, Landmarks(mev = 3, mrv = 10).mv)
    }

    @Test
    fun `MAV defaults to the MEV-MRV midpoint`() {
        assertEquals(15, Landmarks(mev = 8, mrv = 22).mav)
        assertEquals(11, Landmarks(mev = 6, mrv = 15).mav)
    }

    @Test
    fun `landmark ordering is enforced`() {
        assertThrows<IllegalArgumentException> { Landmarks(mev = 8, mrv = 22, mv = 0) }
        assertThrows<IllegalArgumentException> { Landmarks(mev = 8, mrv = 22, mv = 9) }
        assertThrows<IllegalArgumentException> { Landmarks(mev = 8, mrv = 22, mav = 7) }
        assertThrows<IllegalArgumentException> { Landmarks(mev = 8, mrv = 22, mav = 23) }
    }

    @Test
    fun `all fourteen muscles are seeded with valid landmarks`() {
        assertEquals(Muscle.entries.size, DefaultLandmarks.seeds.size)
        for ((muscle, landmarks) in DefaultLandmarks.seeds) {
            assertTrue(landmarks.mv <= landmarks.mev, "MV<=MEV for $muscle")
            assertTrue(landmarks.mev <= landmarks.mav, "MEV<=MAV for $muscle")
            assertTrue(landmarks.mav <= landmarks.mrv, "MAV<=MRV for $muscle")
        }
    }

    @Test
    fun `chest seed matches the DOMAIN table`() {
        val chest = DefaultLandmarks.seeds.getValue(Muscle.CHEST)
        assertEquals(Landmarks(mev = 8, mrv = 22, mv = 4, mav = 16), chest)
    }

    @Test
    fun `beginner scaling shrinks MEV and MRV and re-derives MV and MAV`() {
        val chest = DefaultLandmarks.seeds.getValue(Muscle.CHEST)
        val scaled = chest.scaledFor(Experience.BEGINNER)
        assertEquals(6, scaled.mev) // round(8 * 0.7)
        assertEquals(15, scaled.mrv) // round(22 * 0.7)
        assertEquals(3, scaled.mv) // re-derived: round(6 / 2)
        assertEquals(11, scaled.mav) // re-derived midpoint, stays <= MRV
    }

    @Test
    fun `advanced scaling grows MEV and MRV`() {
        val chest = DefaultLandmarks.seeds.getValue(Muscle.CHEST)
        val scaled = chest.scaledFor(Experience.ADVANCED)
        assertEquals(9, scaled.mev) // round(8 * 1.15)
        assertEquals(25, scaled.mrv) // round(22 * 1.15)
    }

    @Test
    fun `intermediate scaling re-derives but keeps MEV and MRV`() {
        val scaled = Landmarks(mev = 8, mrv = 22, mav = 16).scaledFor(Experience.INTERMEDIATE)
        assertEquals(8, scaled.mev)
        assertEquals(22, scaled.mrv)
    }

    @Test
    fun `scaling floors keep tiny landmarks coherent`() {
        val tiny = Landmarks(mev = 2, mrv = 2).scaledFor(Experience.BEGINNER)
        assertEquals(2, tiny.mev)
        assertEquals(2, tiny.mrv)
        assertTrue(tiny.mv <= tiny.mev && tiny.mav <= tiny.mrv)
    }

    @Test
    fun `seedsFor scales every muscle`() {
        val beginner = DefaultLandmarks.seedsFor(Experience.BEGINNER)
        assertEquals(Muscle.entries.size, beginner.size)
        for ((muscle, landmarks) in beginner) {
            assertTrue(
                landmarks.mrv <= DefaultLandmarks.seeds.getValue(muscle).mrv,
                "beginner MRV should not exceed the seed for $muscle",
            )
        }
    }
}
