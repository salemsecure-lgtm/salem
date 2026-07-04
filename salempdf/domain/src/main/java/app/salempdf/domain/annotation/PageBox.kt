package app.salempdf.domain.annotation

import app.salempdf.domain.render.RectPt

/**
 * Conversion between domain page space and PDF user space.
 *
 * Domain space is the DISPLAYED page: top-left origin, y down, in points,
 * after the page's /Rotate has been applied — exactly what Pdfium rasterizes
 * and what the viewer overlay draws in. PDF user space is the unrotated
 * bottom-left-origin space annotation dictionaries are written in, offset by
 * the crop box origin.
 *
 * This is the single mapper for every write path; keep all flipping and
 * rotation here.
 */
data class PageBox(
    val lowerLeftX: Float,
    val lowerLeftY: Float,
    val widthPt: Float,
    val heightPt: Float,
    /** Page /Rotate, normalized to 0, 90, 180, 270 (clockwise display rotation). */
    val rotation: Int = 0,
) {
    private val turns: Int = ((rotation % 360) + 360) % 360

    /** Width of the page as displayed (swapped for 90/270). */
    val displayedWidthPt: Float get() = if (turns % 180 == 0) widthPt else heightPt

    /** Height of the page as displayed (swapped for 90/270). */
    val displayedHeightPt: Float get() = if (turns % 180 == 0) heightPt else widthPt

    /** Displayed top-left point -> PDF user-space point (x). */
    fun toPdfPoint(point: PointPt): PointPt =
        when (turns) {
            ROTATE_90 -> PointPt(lowerLeftX + point.y, lowerLeftY + point.x)
            ROTATE_180 -> PointPt(lowerLeftX + widthPt - point.x, lowerLeftY + point.y)
            ROTATE_270 -> PointPt(lowerLeftX + widthPt - point.y, lowerLeftY + heightPt - point.x)
            else -> PointPt(lowerLeftX + point.x, lowerLeftY + heightPt - point.y)
        }

    /** PDF user-space point -> displayed top-left point (inverse of [toPdfPoint]). */
    fun toDomainPoint(
        pdfX: Float,
        pdfY: Float,
    ): PointPt {
        val u = pdfX - lowerLeftX
        val v = pdfY - lowerLeftY
        return when (turns) {
            ROTATE_90 -> PointPt(v, u)
            ROTATE_180 -> PointPt(widthPt - u, v)
            ROTATE_270 -> PointPt(heightPt - v, widthPt - u)
            else -> PointPt(u, heightPt - v)
        }
    }

    /** Domain rect -> (llx, lly, urx, ury) in PDF space. */
    fun toPdfRect(rect: RectPt): FloatArray {
        val a = toPdfPoint(PointPt(rect.left, rect.top))
        val b = toPdfPoint(PointPt(rect.right, rect.bottom))
        return floatArrayOf(minOf(a.x, b.x), minOf(a.y, b.y), maxOf(a.x, b.x), maxOf(a.y, b.y))
    }

    /** PDF-space corners (any order) -> domain rect. */
    fun toDomainRect(
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
    ): RectPt {
        val a = toDomainPoint(x1, y1)
        val b = toDomainPoint(x2, y2)
        return RectPt(
            left = minOf(a.x, b.x),
            top = minOf(a.y, b.y),
            right = maxOf(a.x, b.x),
            bottom = maxOf(a.y, b.y),
        )
    }

    companion object {
        private const val ROTATE_90 = 90
        private const val ROTATE_180 = 180
        private const val ROTATE_270 = 270
    }
}
