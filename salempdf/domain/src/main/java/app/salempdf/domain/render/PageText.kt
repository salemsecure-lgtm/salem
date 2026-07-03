package app.salempdf.domain.render

import kotlin.math.abs

/**
 * Extracted text of one page: the character string plus one box per character
 * in top-left-origin page points. Selection logic lives here so it is engine
 * independent and unit-testable.
 */
data class PageText(
    val text: String,
    val charBoxes: List<RectPt>,
) {
    /** Character count usable for selection (text and boxes can disagree at the tail defensively). */
    val length: Int get() = minOf(text.length, charBoxes.size)

    val isEmpty: Boolean get() = length == 0

    /**
     * Index of the character at (or within [tolerancePt] of) the given page point,
     * or null when nothing is close enough.
     */
    fun charIndexNear(
        x: Float,
        y: Float,
        tolerancePt: Float,
    ): Int? {
        var best: Int? = null
        var bestDist = tolerancePt
        for (i in 0 until length) {
            val box = charBoxes[i]
            if (box.width <= 0f && box.height <= 0f) continue
            if (box.contains(x, y)) return i
            val dx = axisDistance(x, box.left, box.right)
            val dy = axisDistance(y, box.top, box.bottom)
            val dist = maxOf(dx, dy)
            if (dist < bestDist) {
                bestDist = dist
                best = i
            }
        }
        return best
    }

    /** Expands [index] to the surrounding whitespace-delimited word. */
    fun wordRangeAt(index: Int): IntRange {
        if (index !in 0 until length) return IntRange.EMPTY
        if (text[index].isWhitespace()) return index..index
        var start = index
        while (start > 0 && !text[start - 1].isWhitespace()) start--
        var end = index
        while (end < length - 1 && !text[end + 1].isWhitespace()) end++
        return start..end
    }

    /** Merges the char boxes of [range] into one rect per text line, for highlight painting. */
    fun selectionRects(range: IntRange): List<RectPt> {
        val rects = mutableListOf<RectPt>()
        var line: RectPt? = null
        for (i in range) {
            if (i !in 0 until length) continue
            val box = charBoxes[i]
            if (box.width <= 0f && box.height <= 0f) continue
            val current = line
            line =
                if (current != null && sameLine(current, box)) {
                    current.union(box)
                } else {
                    current?.let(rects::add)
                    box
                }
        }
        line?.let(rects::add)
        return rects
    }

    fun textIn(range: IntRange): String {
        if (range.isEmpty() || isEmpty) return ""
        val start = range.first.coerceIn(0, length - 1)
        val end = range.last.coerceIn(0, length - 1)
        return text.substring(start, end + 1)
    }

    private fun sameLine(
        a: RectPt,
        b: RectPt,
    ): Boolean {
        val threshold = maxOf(a.height, b.height) / 2f
        return abs(a.centerY - b.centerY) <= threshold
    }

    private fun axisDistance(
        v: Float,
        lo: Float,
        hi: Float,
    ): Float =
        when {
            v < lo -> lo - v
            v > hi -> v - hi
            else -> 0f
        }

    companion object {
        val EMPTY = PageText("", emptyList())
    }
}
