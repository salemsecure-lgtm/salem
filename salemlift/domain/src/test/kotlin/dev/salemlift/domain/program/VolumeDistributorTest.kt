package dev.salemlift.domain.program

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** DOMAIN.md §5.2 volume distribution. */
class VolumeDistributorTest {
    @Test
    fun `distributes evenly with remainder to the earliest sessions`() {
        assertEquals(listOf(7, 6), VolumeDistributor.distribute(13, 2))
        assertEquals(listOf(5, 4, 4), VolumeDistributor.distribute(13, 3))
        assertEquals(listOf(4, 4, 4), VolumeDistributor.distribute(12, 3))
        assertEquals(listOf(8), VolumeDistributor.distribute(8, 1))
    }

    @Test
    fun `small targets leave later sessions empty`() {
        assertEquals(listOf(1, 1, 0), VolumeDistributor.distribute(2, 3))
        assertEquals(listOf(0, 0), VolumeDistributor.distribute(0, 2))
    }

    @Test
    fun `distribute validates inputs`() {
        assertThrows<IllegalArgumentException> { VolumeDistributor.distribute(-1, 2) }
        assertThrows<IllegalArgumentException> { VolumeDistributor.distribute(5, 0) }
    }

    @Test
    fun `increases go plus one at a time to the earliest sessions`() {
        val result = VolumeDistributor.redistribute(listOf(5, 5), 12)
        assertEquals(listOf(6, 6), result.perSession)
        assertFalse(result.capped)
        assertEquals(12, result.total)
    }

    @Test
    fun `per-session increases are capped at plus two`() {
        val result = VolumeDistributor.redistribute(listOf(5, 5), 15)
        assertEquals(listOf(7, 7), result.perSession)
        assertTrue(result.capped)
        assertEquals(14, result.total)
    }

    @Test
    fun `single-session muscles cap a plus-three week at plus two`() {
        val result = VolumeDistributor.redistribute(listOf(10), 13)
        assertEquals(listOf(12), result.perSession)
        assertTrue(result.capped)
    }

    @Test
    fun `decreases come off the latest sessions first`() {
        val result = VolumeDistributor.redistribute(listOf(7, 6), 10)
        assertEquals(listOf(7, 3), result.perSession)
        assertFalse(result.capped)
    }

    @Test
    fun `large decreases drain sessions from the back without going negative`() {
        val result = VolumeDistributor.redistribute(listOf(4, 4, 4), 3)
        assertEquals(listOf(3, 0, 0), result.perSession)
        assertEquals(3, result.total)
    }

    @Test
    fun `unchanged target returns the previous distribution`() {
        val result = VolumeDistributor.redistribute(listOf(6, 5), 11)
        assertEquals(listOf(6, 5), result.perSession)
        assertFalse(result.capped)
    }

    @Test
    fun `redistribute validates inputs`() {
        assertThrows<IllegalArgumentException> { VolumeDistributor.redistribute(emptyList(), 5) }
        assertThrows<IllegalArgumentException> { VolumeDistributor.redistribute(listOf(-1, 3), 5) }
        assertThrows<IllegalArgumentException> { VolumeDistributor.redistribute(listOf(3), -1) }
    }
}
