package dev.salemlift.domain

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class E1rmTest {
    @Test
    fun `DOMAIN section 8 example - 100kg x8 at 2 RIR is 133point3`() {
        assertEquals(133.33, e1rm(100.0, 8, 2), 0.01)
    }

    @Test
    fun `DOMAIN section 8 example - 60kg x12 at 0 RIR is 84`() {
        assertEquals(84.0, e1rm(60.0, 12, 0), 0.001)
    }

    @Test
    fun `single rep at 0 RIR adds one thirtieth`() {
        assertEquals(103.33, e1rm(100.0, 1, 0), 0.01)
    }

    @Test
    fun `invalid inputs are rejected`() {
        assertThrows<IllegalArgumentException> { e1rm(0.0, 8, 2) }
        assertThrows<IllegalArgumentException> { e1rm(100.0, 0, 2) }
        assertThrows<IllegalArgumentException> { e1rm(100.0, 8, -1) }
    }
}
