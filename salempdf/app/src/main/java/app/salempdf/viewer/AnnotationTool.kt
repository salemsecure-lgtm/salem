package app.salempdf.viewer

import app.salempdf.domain.annotation.MarkupKind
import app.salempdf.domain.annotation.PointPt
import app.salempdf.domain.annotation.ShapeKind

/** Where a tap-to-place tool wants to create an annotation. */
data class PlacementRequest(val tool: AnnotationTool, val pageIndex: Int, val point: PointPt)

/** The active annotation tool; null means pan/select mode. */
enum class AnnotationTool(val label: String) {
    HIGHLIGHT("Highlight"),
    UNDERLINE("Underline"),
    STRIKEOUT("Strikeout"),
    INK("Draw"),
    RECT("Box"),
    OVAL("Oval"),
    LINE("Line"),
    ARROW("Arrow"),
    NOTE("Note"),
    FREETEXT("Text"),
    SIGNATURE("Sign"),
    ;

    val markupKind: MarkupKind?
        get() =
            when (this) {
                HIGHLIGHT -> MarkupKind.HIGHLIGHT
                UNDERLINE -> MarkupKind.UNDERLINE
                STRIKEOUT -> MarkupKind.STRIKEOUT
                else -> null
            }

    val shapeKind: ShapeKind?
        get() =
            when (this) {
                RECT -> ShapeKind.RECT
                OVAL -> ShapeKind.OVAL
                LINE -> ShapeKind.LINE
                ARROW -> ShapeKind.ARROW
                else -> null
            }

    /** Default color per tool; the palette can override. */
    val defaultColor: Int
        get() =
            when (this) {
                HIGHLIGHT -> AMBER
                UNDERLINE, STRIKEOUT -> RED
                else -> INDIGO
            }

    val defaultOpacity: Float
        get() = if (this == HIGHLIGHT) HIGHLIGHT_OPACITY else 1f

    companion object {
        const val AMBER = 0xFBBF24
        const val RED = 0xDC2626
        const val INDIGO = 0x4F46E5
        const val BLACK = 0x1F2937
        const val GREEN = 0x059669
        const val HIGHLIGHT_OPACITY = 0.4f

        val PALETTE = listOf(AMBER, RED, INDIGO, GREEN, BLACK)
        val STROKE_WIDTHS_PT = listOf(1.5f, 3f, 6f)
    }
}
