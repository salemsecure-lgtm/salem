package app.salempdf.domain.model

/**
 * Size of a PDF page in PDF user-space points (1/72 inch), as reported by the
 * render engine. The single source of truth for page geometry across modules.
 */
data class PageSize(val widthPt: Float, val heightPt: Float) {
    init {
        require(widthPt > 0f && heightPt > 0f) { "Page dimensions must be positive: $widthPt x $heightPt" }
    }

    val aspectRatio: Float get() = widthPt / heightPt
}
