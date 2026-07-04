package app.salempdf.domain.annotation

import app.salempdf.domain.render.RectPt
import org.junit.Assert.assertEquals
import org.junit.Test

class PageBoxTest {
    /** Portrait page 100 x 200 pt with a non-zero crop origin. */
    private fun box(rotation: Int) =
        PageBox(lowerLeftX = 10f, lowerLeftY = 20f, widthPt = 100f, heightPt = 200f, rotation = rotation)

    private fun assertPoint(
        expected: PointPt,
        actual: PointPt,
    ) {
        assertEquals(expected.x, actual.x, 0.001f)
        assertEquals(expected.y, actual.y, 0.001f)
    }

    @Test
    fun `rotation swaps displayed dimensions`() {
        assertEquals(100f, box(0).displayedWidthPt, 0f)
        assertEquals(200f, box(90).displayedWidthPt, 0f)
        assertEquals(100f, box(90).displayedHeightPt, 0f)
        assertEquals(100f, box(180).displayedWidthPt, 0f)
        assertEquals(200f, box(270).displayedWidthPt, 0f)
    }

    @Test
    fun `unrotated top-left maps to pdf upper-left`() {
        assertPoint(PointPt(10f, 220f), box(0).toPdfPoint(PointPt(0f, 0f)))
    }

    @Test
    fun `rotate 90 corners map correctly`() {
        val b = box(90)
        // Displayed top-left corresponds to the unrotated bottom-left (10, 20).
        assertPoint(PointPt(10f, 20f), b.toPdfPoint(PointPt(0f, 0f)))
        // Displayed top-right (x = displayed width 200) -> unrotated top-left.
        assertPoint(PointPt(10f, 220f), b.toPdfPoint(PointPt(200f, 0f)))
        // Displayed bottom-left -> unrotated bottom-right.
        assertPoint(PointPt(110f, 20f), b.toPdfPoint(PointPt(0f, 100f)))
    }

    @Test
    fun `rotate 180 corners map correctly`() {
        val b = box(180)
        // Displayed top-left -> unrotated bottom-right corner region.
        assertPoint(PointPt(110f, 20f), b.toPdfPoint(PointPt(0f, 0f)))
        assertPoint(PointPt(10f, 220f), b.toPdfPoint(PointPt(100f, 200f)))
    }

    @Test
    fun `rotate 270 corners map correctly`() {
        val b = box(270)
        // Displayed top-left -> unrotated top-right.
        assertPoint(PointPt(110f, 220f), b.toPdfPoint(PointPt(0f, 0f)))
        // Displayed bottom-right -> unrotated bottom-left.
        assertPoint(PointPt(10f, 20f), b.toPdfPoint(PointPt(200f, 100f)))
    }

    @Test
    fun `toDomainPoint inverts toPdfPoint for all rotations`() {
        val samples =
            listOf(
                PointPt(0f, 0f),
                PointPt(37.5f, 12.25f),
                PointPt(99f, 180f),
            )
        for (rotation in listOf(0, 90, 180, 270)) {
            val b = box(rotation)
            for (point in samples) {
                val within =
                    PointPt(
                        point.x.coerceAtMost(b.displayedWidthPt),
                        point.y.coerceAtMost(b.displayedHeightPt),
                    )
                val roundTripped = b.toPdfPoint(within).let { b.toDomainPoint(it.x, it.y) }
                assertPoint(within, roundTripped)
            }
        }
    }

    @Test
    fun `rect round-trips through pdf space for all rotations`() {
        val rect = RectPt(5f, 10f, 60f, 90f)
        for (rotation in listOf(0, 90, 180, 270)) {
            val b = box(rotation)
            val pdf = b.toPdfRect(rect)
            val back = b.toDomainRect(pdf[0], pdf[1], pdf[2], pdf[3])
            assertEquals(rect.left, back.left, 0.001f)
            assertEquals(rect.top, back.top, 0.001f)
            assertEquals(rect.right, back.right, 0.001f)
            assertEquals(rect.bottom, back.bottom, 0.001f)
        }
    }

    @Test
    fun `negative rotation normalizes`() {
        // -90 == 270.
        val b = PageBox(0f, 0f, 100f, 200f, rotation = -90)
        assertEquals(200f, b.displayedWidthPt, 0f)
    }
}
