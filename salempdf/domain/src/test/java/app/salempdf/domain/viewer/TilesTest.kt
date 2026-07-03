package app.salempdf.domain.viewer

import app.salempdf.domain.model.PageSize
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TilesTest {
    private val square = PageSize(1000f, 1000f)

    private fun layout(zoom: Float) =
        DocumentLayout(List(3) { square }, viewportWidthPx = 1000f, zoom = zoom, gapPx = 0f)

    @Test
    fun `page fully visible at bucket equals zoom covers page with grid`() {
        val l = layout(zoom = 1f)
        val tiles =
            visibleTiles(
                layout = l,
                pageIndex = 0,
                scrollY = 0f,
                offsetX = 0f,
                viewportWidthPx = 1000f,
                viewportHeightPx = 1000f,
                bucket = 1f,
            )
        // 1000x1000 page in 512px tiles -> 2x2 grid.
        assertEquals(4, tiles.size)
        val last = tiles.last()
        assertEquals(512, last.leftPx)
        assertEquals(1000 - 512, last.widthPx)
    }

    @Test
    fun `only viewport-intersecting tiles are produced when zoomed`() {
        val l = layout(zoom = 4f)
        val tiles =
            visibleTiles(
                layout = l,
                pageIndex = 0,
                scrollY = 0f,
                offsetX = 0f,
                viewportWidthPx = 1000f,
                viewportHeightPx = 1000f,
                bucket = 4f,
            )
        // Page is 4000px in content space; viewport sees the top-left 1000px -> 2x2 of 512 tiles.
        assertEquals(4, tiles.size)
        assertTrue(tiles.all { it.leftPx < 1024 && it.topPx < 1024 })
        assertEquals(4000, tiles.first().pageWidthPx)
    }

    @Test
    fun `page outside the viewport yields no tiles`() {
        val l = layout(zoom = 1f)
        val tiles =
            visibleTiles(
                layout = l,
                pageIndex = 2,
                scrollY = 0f,
                offsetX = 0f,
                viewportWidthPx = 1000f,
                viewportHeightPx = 800f,
                bucket = 1f,
            )
        assertEquals(0, tiles.size)
    }

    @Test
    fun `bucket above zoom scales tile coordinates`() {
        val l = layout(zoom = 1.4f)
        val tiles =
            visibleTiles(
                layout = l,
                pageIndex = 0,
                scrollY = 0f,
                offsetX = 0f,
                viewportWidthPx = 1000f,
                viewportHeightPx = 500f,
                bucket = 1.5f,
            )
        assertTrue(tiles.isNotEmpty())
        assertEquals(1500, tiles.first().pageWidthPx)
        // Tiles never exceed the page raster bounds.
        assertTrue(tiles.all { it.leftPx + it.widthPx <= it.pageWidthPx })
        assertTrue(tiles.all { it.topPx + it.heightPx <= it.pageHeightPx })
    }
}
