package app.salempdf.domain.render

import kotlin.math.max
import kotlin.math.min

/**
 * Axis-aligned rectangle in page space: PDF points with a TOP-LEFT origin.
 *
 * Pdfium/PDF user space is bottom-left origin; adapters must flip the y axis
 * before constructing a [RectPt]. All domain code assumes top-left origin so
 * screen mapping is a pure scale + translate.
 */
data class RectPt(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
) {
    val width: Float get() = right - left
    val height: Float get() = bottom - top
    val centerY: Float get() = (top + bottom) / 2f

    fun contains(
        x: Float,
        y: Float,
    ): Boolean = x >= left && x <= right && y >= top && y <= bottom

    fun union(other: RectPt): RectPt =
        RectPt(
            left = min(left, other.left),
            top = min(top, other.top),
            right = max(right, other.right),
            bottom = max(bottom, other.bottom),
        )

    companion object {
        val EMPTY = RectPt(0f, 0f, 0f, 0f)
    }
}
