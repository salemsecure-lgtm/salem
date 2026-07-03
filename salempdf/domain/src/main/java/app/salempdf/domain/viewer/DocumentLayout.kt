package app.salempdf.domain.viewer

import app.salempdf.domain.model.PageSize

/**
 * Pure layout of a document as a vertical strip of pages, each scaled to the
 * full content width (fit-width), separated by [gapPx]. All coordinates are
 * "content pixels" at the current zoom: page width = viewportWidthPx * zoom.
 *
 * Pages whose size is not yet known (lazy size loading) fall back to
 * [fallbackSize] so layout stays total and stable while sizes stream in.
 */
class DocumentLayout(
    pageSizes: List<PageSize?>,
    val viewportWidthPx: Float,
    val zoom: Float,
    val gapPx: Float,
    fallbackSize: PageSize = DEFAULT_PAGE,
) {
    val pageCount: Int = pageSizes.size
    val pageWidthPx: Float = viewportWidthPx * zoom

    private val sizes: List<PageSize> = pageSizes.map { it ?: fallbackSize }
    private val heights = FloatArray(pageCount) { i -> pageWidthPx / sizes[i].aspectRatio }
    private val tops =
        FloatArray(pageCount).also { tops ->
            var y = gapPx
            for (i in 0 until pageCount) {
                tops[i] = y
                y += heights[i] + gapPx
            }
        }

    val totalHeightPx: Float =
        if (pageCount == 0) 0f else tops[pageCount - 1] + heights[pageCount - 1] + gapPx

    fun pageTopPx(index: Int): Float = tops[index]

    fun pageHeightPx(index: Int): Float = heights[index]

    /** Scale from page points to content pixels for [index]. */
    fun pageScale(index: Int): Float = pageWidthPx / sizes[index].widthPt

    fun pageSizePt(index: Int): PageSize = sizes[index]

    /** Pages intersecting the viewport at [scrollY], as an index range (empty doc -> empty range). */
    fun visiblePages(
        scrollY: Float,
        viewportHeightPx: Float,
    ): IntRange {
        if (pageCount == 0) return IntRange.EMPTY
        val first = pageAtY(scrollY)
        var last = first
        while (last + 1 < pageCount && tops[last + 1] < scrollY + viewportHeightPx) last++
        return first..last
    }

    /** Index of the page containing (or nearest below) content-y [y]. */
    fun pageAtY(y: Float): Int {
        if (pageCount == 0) return 0
        var lo = 0
        var hi = pageCount - 1
        while (lo < hi) {
            val mid = (lo + hi + 1) / 2
            if (tops[mid] <= y) lo = mid else hi = mid - 1
        }
        // lo is the last page starting at or above y; step forward if y is past its bottom gap.
        return if (y > tops[lo] + heights[lo] && lo + 1 < pageCount) lo + 1 else lo
    }

    /** Max scroll so the last page bottom aligns with the viewport bottom (never negative). */
    fun maxScrollY(viewportHeightPx: Float): Float = (totalHeightPx - viewportHeightPx).coerceAtLeast(0f)

    companion object {
        /** US Letter in points; neutral placeholder while real sizes load. */
        val DEFAULT_PAGE = PageSize(612f, 792f)
    }
}
