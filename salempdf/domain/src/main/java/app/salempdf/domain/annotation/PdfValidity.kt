package app.salempdf.domain.annotation

import com.tom_roush.pdfbox.pdmodel.PDDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

/**
 * Structural validity check every write path must pass before an output
 * replaces the user's file (SPEC: never corrupt the source document).
 * The renderer-level spot check (rasterize page 1) is layered on in :app.
 */
object PdfValidity {
    /** Reopens [file] and returns its page count, or failure if it cannot be parsed. */
    suspend fun check(
        file: File,
        expectedPageCount: Int? = null,
    ): Result<Int> =
        withContext(Dispatchers.IO) {
            runCatching {
                PDDocument.load(file).use { doc ->
                    val pages = doc.numberOfPages
                    if (pages <= 0) throw IOException("Document has no pages")
                    if (expectedPageCount != null && pages != expectedPageCount) {
                        throw IOException("Expected $expectedPageCount pages, found $pages")
                    }
                    pages
                }
            }
        }
}
