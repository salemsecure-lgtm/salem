package app.salempdf.domain.pageops

/**
 * In-document page operations, applied in list order by [PageOpsWriter].
 * Indices refer to the document state at the time the op executes.
 */
sealed interface PageOp {
    /** Rotates [pages] by [clockwiseTurns] * 90 degrees (persisted in each page's /Rotate). */
    data class Rotate(val pages: List<Int>, val clockwiseTurns: Int = 1) : PageOp {
        init {
            require(pages.isNotEmpty()) { "Rotate needs at least one page" }
        }
    }

    data class Delete(val pages: List<Int>) : PageOp {
        init {
            require(pages.isNotEmpty()) { "Delete needs at least one page" }
        }
    }

    /** Reorders the document to [order] — a full permutation of the current indices. */
    data class Reorder(val order: List<Int>) : PageOp {
        init {
            require(order.sorted() == order.indices.toList()) { "Order must be a permutation of 0..${order.size - 1}" }
        }
    }

    /** Moves one page from [from] to position [to] (both in current indices). */
    data class Move(val from: Int, val to: Int) : PageOp

    /** Inserts a blank page before index [at] (append when [at] >= page count). */
    data class InsertBlank(val at: Int, val widthPt: Float = LETTER_WIDTH_PT, val heightPt: Float = LETTER_HEIGHT_PT) :
        PageOp {
        init {
            require(widthPt > 0f && heightPt > 0f) { "Blank page needs positive dimensions" }
        }
    }

    companion object {
        const val LETTER_WIDTH_PT = 612f
        const val LETTER_HEIGHT_PT = 792f
    }
}

/** Outcome of a compress run; sizes in bytes so the UI can report the real ratio. */
data class CompressResult(val bytesBefore: Long, val bytesAfter: Long, val imagesRecompressed: Int) {
    val savedFraction: Float
        get() = if (bytesBefore <= 0) 0f else 1f - bytesAfter.toFloat() / bytesBefore.toFloat()
}
