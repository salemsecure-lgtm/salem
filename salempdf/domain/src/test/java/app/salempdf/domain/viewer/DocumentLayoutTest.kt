package app.salempdf.domain.viewer

import app.salempdf.domain.model.PageSize
import org.junit.Assert.assertEquals
import org.junit.Test

class DocumentLayoutTest {
    private val a4 = PageSize(595f, 842f)

    private fun layout(
        pages: Int = 10,
        viewportWidth: Float = 1000f,
        zoom: Float = 1f,
        gap: Float = 10f,
    ) = DocumentLayout(List(pages) { a4 }, viewportWidth, zoom, gap)

    @Test
    fun `pages stack vertically with gaps`() {
        val l = layout(pages = 3)
        val pageHeight = 1000f / a4.aspectRatio
        assertEquals(10f, l.pageTopPx(0), 0.01f)
        assertEquals(10f + pageHeight + 10f, l.pageTopPx(1), 0.01f)
        assertEquals(3 * pageHeight + 4 * 10f, l.totalHeightPx, 0.01f)
    }

    @Test
    fun `zoom scales page width and heights`() {
        val l = layout(zoom = 2f)
        assertEquals(2000f, l.pageWidthPx, 0.01f)
        assertEquals(2000f / a4.aspectRatio, l.pageHeightPx(0), 0.01f)
    }

    @Test
    fun `visiblePages returns pages intersecting the viewport`() {
        val l = layout(pages = 200)
        val pageStride = l.pageHeightPx(0) + 10f
        // Scrolled to page 50 exactly; viewport shows ~1.5 pages.
        val scrollY = l.pageTopPx(50)
        val visible = l.visiblePages(scrollY, viewportHeightPx = pageStride * 1.5f)
        assertEquals(50, visible.first)
        assertEquals(51, visible.last)
    }

    @Test
    fun `visiblePages at document start and end stay in bounds`() {
        val l = layout(pages = 5)
        assertEquals(0, l.visiblePages(0f, 100f).first)
        val atEnd = l.visiblePages(l.totalHeightPx, 1000f)
        assertEquals(4, atEnd.last)
    }

    @Test
    fun `pageAtY lands in gap picks following page`() {
        val l = layout(pages = 3)
        val inGapBelowFirst = l.pageTopPx(0) + l.pageHeightPx(0) + 5f
        assertEquals(1, l.pageAtY(inGapBelowFirst))
    }

    @Test
    fun `unknown sizes use fallback so layout stays total`() {
        val l = DocumentLayout(listOf(a4, null, null), 1000f, 1f, 10f)
        assertEquals(3, l.pageCount)
        assertEquals(1000f / DocumentLayout.DEFAULT_PAGE.aspectRatio, l.pageHeightPx(1), 0.01f)
    }

    @Test
    fun `maxScrollY never negative for short documents`() {
        val l = layout(pages = 1)
        assertEquals(0f, l.maxScrollY(viewportHeightPx = 10_000f), 0.01f)
    }

    @Test
    fun `pageScale converts points to content pixels`() {
        val l = layout(zoom = 2f)
        assertEquals(2000f / 595f, l.pageScale(0), 0.0001f)
    }
}
