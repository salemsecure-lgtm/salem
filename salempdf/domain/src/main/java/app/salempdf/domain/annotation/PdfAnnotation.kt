package app.salempdf.domain.annotation

import app.salempdf.domain.render.RectPt

/** A point in top-left-origin page points (see [RectPt] for the convention). */
data class PointPt(val x: Float, val y: Float)

enum class MarkupKind { HIGHLIGHT, UNDERLINE, STRIKEOUT }

enum class ShapeKind { RECT, OVAL, LINE, ARROW }

/**
 * Engine-independent annotation model. Every annotation persists into the PDF
 * as a standard annotation dictionary with an appearance stream (see
 * ARCHITECTURE.md — the document, not Room, is the source of truth).
 *
 * [id] identity: annotations created in-session get "new:<uuid>"; annotations
 * parsed back from a Salem-written file carry the /NM value ("salempdf:<uuid>").
 * Colors are packed ARGB (alpha carried separately as [opacity] 0..1).
 */
sealed interface PdfAnnotation {
    val id: String
    val pageIndex: Int
    val colorRgb: Int
    val opacity: Float

    /** Bounding box in top-left-origin page points, for hit-testing and handles. */
    val bounds: RectPt

    data class TextMarkup(
        override val id: String,
        override val pageIndex: Int,
        override val colorRgb: Int,
        override val opacity: Float,
        val kind: MarkupKind,
        /** One rect per marked text line, top-left-origin page points. */
        val lineRects: List<RectPt>,
    ) : PdfAnnotation {
        override val bounds: RectPt get() = lineRects.reduce(RectPt::union)
    }

    data class Ink(
        override val id: String,
        override val pageIndex: Int,
        override val colorRgb: Int,
        override val opacity: Float,
        val strokes: List<List<PointPt>>,
        val strokeWidthPt: Float,
    ) : PdfAnnotation {
        override val bounds: RectPt
            get() {
                val points = strokes.flatten()
                val pad = strokeWidthPt / 2f
                return RectPt(
                    left = (points.minOf { it.x }) - pad,
                    top = (points.minOf { it.y }) - pad,
                    right = (points.maxOf { it.x }) + pad,
                    bottom = (points.maxOf { it.y }) + pad,
                )
            }
    }

    data class Shape(
        override val id: String,
        override val pageIndex: Int,
        override val colorRgb: Int,
        override val opacity: Float,
        val kind: ShapeKind,
        /** RECT/OVAL: the box. LINE/ARROW: start=(left,top) end=(right,bottom), may be unordered. */
        val start: PointPt,
        val end: PointPt,
        val strokeWidthPt: Float,
    ) : PdfAnnotation {
        override val bounds: RectPt
            get() {
                val pad = strokeWidthPt / 2f
                return RectPt(
                    left = minOf(start.x, end.x) - pad,
                    top = minOf(start.y, end.y) - pad,
                    right = maxOf(start.x, end.x) + pad,
                    bottom = maxOf(start.y, end.y) + pad,
                )
            }
    }

    data class Note(
        override val id: String,
        override val pageIndex: Int,
        override val colorRgb: Int,
        override val opacity: Float,
        val at: PointPt,
        val contents: String,
    ) : PdfAnnotation {
        override val bounds: RectPt
            get() = RectPt(at.x, at.y, at.x + ICON_SIZE_PT, at.y + ICON_SIZE_PT)

        companion object {
            const val ICON_SIZE_PT = 20f
        }
    }

    data class FreeText(
        override val id: String,
        override val pageIndex: Int,
        override val colorRgb: Int,
        override val opacity: Float,
        val rect: RectPt,
        val text: String,
        val fontSizePt: Float,
    ) : PdfAnnotation {
        override val bounds: RectPt get() = rect
    }

    data class Stamp(
        override val id: String,
        override val pageIndex: Int,
        override val colorRgb: Int,
        override val opacity: Float,
        val rect: RectPt,
        /** PNG-encoded image (signatures use a transparent background). */
        val pngBytes: ByteArray,
    ) : PdfAnnotation {
        override val bounds: RectPt get() = rect

        override fun equals(other: Any?): Boolean = other is Stamp && other.id == id && other.rect == rect

        override fun hashCode(): Int = 31 * id.hashCode() + rect.hashCode()
    }

    /** Returns a copy translated by (dx, dy) page points. */
    fun movedBy(
        dx: Float,
        dy: Float,
    ): PdfAnnotation =
        when (this) {
            is TextMarkup -> copy(lineRects = lineRects.map { it.translated(dx, dy) })
            is Ink -> copy(strokes = strokes.map { s -> s.map { PointPt(it.x + dx, it.y + dy) } })
            is Shape -> copy(start = PointPt(start.x + dx, start.y + dy), end = PointPt(end.x + dx, end.y + dy))
            is Note -> copy(at = PointPt(at.x + dx, at.y + dy))
            is FreeText -> copy(rect = rect.translated(dx, dy))
            is Stamp -> copy(rect = rect.translated(dx, dy))
        }
}

private fun RectPt.translated(
    dx: Float,
    dy: Float,
): RectPt = RectPt(left + dx, top + dy, right + dx, bottom + dy)
