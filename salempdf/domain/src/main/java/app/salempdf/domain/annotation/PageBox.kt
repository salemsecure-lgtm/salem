package app.salempdf.domain.annotation

import app.salempdf.domain.render.RectPt

/**
 * Conversion between domain page space (top-left origin, y down — see
 * [RectPt]) and PDF user space (bottom-left origin, y up, offset by the crop
 * box origin). This is the single mapper for every write path; keep all
 * flipping here.
 */
data class PageBox(
    val lowerLeftX: Float,
    val lowerLeftY: Float,
    val widthPt: Float,
    val heightPt: Float,
) {
    fun toPdfX(x: Float): Float = lowerLeftX + x

    fun toPdfY(y: Float): Float = lowerLeftY + heightPt - y

    fun toDomainX(pdfX: Float): Float = pdfX - lowerLeftX

    fun toDomainY(pdfY: Float): Float = lowerLeftY + heightPt - pdfY

    /** Domain rect -> (llx, lly, urx, ury) in PDF space. */
    fun toPdfRect(rect: RectPt): FloatArray =
        floatArrayOf(toPdfX(rect.left), toPdfY(rect.bottom), toPdfX(rect.right), toPdfY(rect.top))

    /** PDF-space corners (any order) -> domain rect. */
    fun toDomainRect(
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
    ): RectPt =
        RectPt(
            left = minOf(toDomainX(x1), toDomainX(x2)),
            top = minOf(toDomainY(y1), toDomainY(y2)),
            right = maxOf(toDomainX(x1), toDomainX(x2)),
            bottom = maxOf(toDomainY(y1), toDomainY(y2)),
        )
}
