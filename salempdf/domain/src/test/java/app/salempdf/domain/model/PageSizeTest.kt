package app.salempdf.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class PageSizeTest {
    @Test
    fun `aspect ratio is width over height`() {
        val a4 = PageSize(widthPt = 595f, heightPt = 842f)
        assertEquals(595f / 842f, a4.aspectRatio, 1e-6f)
    }

    @Test
    fun `rejects non-positive dimensions`() {
        assertThrows(IllegalArgumentException::class.java) {
            PageSize(widthPt = 0f, heightPt = 842f)
        }
        assertThrows(IllegalArgumentException::class.java) {
            PageSize(widthPt = 595f, heightPt = -1f)
        }
    }
}
