package dev.salemlift.app.common

import dev.salemlift.domain.model.Muscle
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class FormatTest {
    @Test
    fun `countdown formats mm-ss and rounds partial seconds up`() {
        assertEquals("2:30", formatCountdown(150_000))
        assertEquals("2:30", formatCountdown(149_400))
        assertEquals("0:01", formatCountdown(1))
        assertEquals("0:00", formatCountdown(0))
    }

    @Test
    fun `weights drop the decimal only when whole`() {
        assertEquals("60", formatWeight(60.0))
        assertEquals("62.5", formatWeight(62.5))
    }

    @Test
    fun `muscle names render in title case`() {
        assertEquals("Chest", Muscle.CHEST.displayName())
        assertEquals("Front Delts", Muscle.FRONT_DELTS.displayName())
    }
}
