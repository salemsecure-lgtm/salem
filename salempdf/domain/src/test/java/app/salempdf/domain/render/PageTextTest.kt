package app.salempdf.domain.render

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PageTextTest {
    /**
     * "ab cd" laid out on one line, then "ef" on a second line. Boxes are, in
     * order: 'a', 'b', space, 'c', 'd' on line one; 'e', 'f' on line two.
     */
    private fun sample(): PageText {
        val boxes =
            listOf(
                RectPt(0f, 0f, 10f, 12f),
                RectPt(10f, 0f, 20f, 12f),
                RectPt(20f, 0f, 25f, 12f),
                RectPt(25f, 0f, 35f, 12f),
                RectPt(35f, 0f, 45f, 12f),
                RectPt(0f, 20f, 10f, 32f),
                RectPt(10f, 20f, 20f, 32f),
            )
        return PageText("ab cdef", boxes)
    }

    @Test
    fun `charIndexNear finds containing box`() {
        assertEquals(3, sample().charIndexNear(30f, 6f, tolerancePt = 4f))
    }

    @Test
    fun `charIndexNear respects tolerance`() {
        assertNull(sample().charIndexNear(200f, 200f, tolerancePt = 8f))
        assertEquals(6, sample().charIndexNear(22f, 26f, tolerancePt = 8f))
    }

    @Test
    fun `wordRangeAt expands to whitespace boundaries`() {
        assertEquals(0..1, sample().wordRangeAt(1))
        assertEquals(3..6, sample().wordRangeAt(4))
    }

    @Test
    fun `selectionRects merges per line`() {
        val rects = sample().selectionRects(0..6)
        assertEquals(2, rects.size)
        assertEquals(RectPt(0f, 0f, 45f, 12f), rects[0])
        assertEquals(RectPt(0f, 20f, 20f, 32f), rects[1])
    }

    @Test
    fun `textIn returns the selected substring`() {
        assertEquals("b cd", sample().textIn(1..4))
        assertEquals("", PageText.EMPTY.textIn(0..3))
    }

    @Test
    fun `length is defensive when boxes and text disagree`() {
        val t = PageText("abc", listOf(RectPt(0f, 0f, 1f, 1f)))
        assertEquals(1, t.length)
    }
}
