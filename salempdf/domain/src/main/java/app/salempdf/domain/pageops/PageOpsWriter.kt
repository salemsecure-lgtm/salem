package app.salempdf.domain.pageops

import android.graphics.Bitmap
import com.tom_roush.pdfbox.cos.COSName
import com.tom_roush.pdfbox.multipdf.PDFMergerUtility
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDResources
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.graphics.image.JPEGFactory
import com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * DOCOPS write paths over PdfBox. Structural rule (SPEC): callers never point
 * [target] at the user's live file — they write to a temp file, run the
 * validity check, then swap. Page objects move with their annotations and
 * resources, so reorder/rotate/delete preserve everything page-local.
 */
class PageOpsWriter {
    suspend fun applyOps(
        source: File,
        target: File,
        ops: List<PageOp>,
    ): Unit =
        withContext(Dispatchers.IO) {
            PDDocument.load(source).use { doc ->
                for (op in ops) {
                    applyOp(doc, op)
                }
                doc.save(target)
            }
        }

    /** Copies [pages] (in the given order) into a fresh document at [target]. */
    suspend fun extractPages(
        source: File,
        target: File,
        pages: List<Int>,
    ): Unit =
        withContext(Dispatchers.IO) {
            require(pages.isNotEmpty()) { "Extract needs at least one page" }
            PDDocument.load(source).use { doc ->
                PDDocument().use { out ->
                    for (index in pages) {
                        out.importPage(doc.getPage(index))
                    }
                    out.save(target)
                }
            }
        }

    /** Concatenates [sources] in order into [target]. */
    suspend fun merge(
        sources: List<File>,
        target: File,
    ): Unit =
        withContext(Dispatchers.IO) {
            require(sources.size >= 2) { "Merge needs at least two documents" }
            val merger = PDFMergerUtility()
            sources.forEach(merger::addSource)
            merger.destinationFileName = target.absolutePath
            merger.mergeDocuments(null)
        }

    /**
     * Rewrites [source] with large embedded images downsampled to
     * [maxDimensionPx] and re-encoded as JPEG at [quality]. Images carrying
     * transparency (soft masks) are left untouched — JPEG would drop alpha.
     */
    suspend fun compress(
        source: File,
        target: File,
        quality: Float = DEFAULT_JPEG_QUALITY,
        maxDimensionPx: Int = DEFAULT_MAX_IMAGE_PX,
    ): CompressResult =
        withContext(Dispatchers.IO) {
            var recompressed = 0
            PDDocument.load(source).use { doc ->
                for (page in doc.pages) {
                    val resources = page.resources ?: continue
                    for (name in resources.xObjectNames) {
                        if (recompressImage(doc, resources, name, quality, maxDimensionPx)) recompressed++
                    }
                }
                doc.save(target)
            }
            CompressResult(source.length(), target.length(), recompressed)
        }

    /** Recompresses one image XObject in place; false when it should be left untouched. */
    private fun recompressImage(
        doc: PDDocument,
        resources: PDResources,
        name: COSName,
        quality: Float,
        maxDimensionPx: Int,
    ): Boolean {
        val image = resources.getXObject(name) as? PDImageXObject ?: return false
        if (image.softMask != null) return false
        if (image.width <= COMPRESS_MIN_PX && image.height <= COMPRESS_MIN_PX) return false
        val bitmap = runCatching { image.image }.getOrNull() ?: return false
        val scaled = downscale(bitmap, maxDimensionPx)
        resources.put(name, JPEGFactory.createFromImage(doc, scaled, quality))
        return true
    }

    private fun applyOp(
        doc: PDDocument,
        op: PageOp,
    ) {
        when (op) {
            is PageOp.Rotate ->
                for (index in op.pages) {
                    val page = doc.getPage(index)
                    page.rotation = normalizedDegrees(page.rotation + op.clockwiseTurns * QUARTER_TURN_DEG)
                }
            is PageOp.Delete ->
                op.pages.distinct().sortedDescending().forEach(doc::removePage)
            is PageOp.Reorder -> reorder(doc, op.order)
            is PageOp.Move -> {
                val count = doc.numberOfPages
                val from = op.from.coerceIn(0, count - 1)
                val to = op.to.coerceIn(0, count - 1)
                if (from != to) {
                    val order = (0 until count).toMutableList()
                    order.removeAt(from)
                    order.add(to, from)
                    reorder(doc, order)
                }
            }
            is PageOp.InsertBlank -> {
                val blank = PDPage(PDRectangle(op.widthPt, op.heightPt))
                if (op.at >= doc.numberOfPages) {
                    doc.addPage(blank)
                } else {
                    doc.pages.insertBefore(blank, doc.getPage(op.at.coerceAtLeast(0)))
                }
            }
        }
    }

    private fun reorder(
        doc: PDDocument,
        order: List<Int>,
    ) {
        require(order.size == doc.numberOfPages) { "Order must cover every page" }
        val pages = (0 until doc.numberOfPages).map(doc::getPage)
        pages.forEach { doc.pages.remove(it) }
        order.forEach { doc.addPage(pages[it]) }
    }

    private fun normalizedDegrees(degrees: Int): Int = ((degrees % FULL_TURN_DEG) + FULL_TURN_DEG) % FULL_TURN_DEG

    private fun downscale(
        bitmap: Bitmap,
        maxDimensionPx: Int,
    ): Bitmap {
        val largest = maxOf(bitmap.width, bitmap.height)
        if (largest <= maxDimensionPx) return bitmap
        val scale = maxDimensionPx.toFloat() / largest
        return Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * scale).toInt().coerceAtLeast(1),
            (bitmap.height * scale).toInt().coerceAtLeast(1),
            true,
        )
    }

    companion object {
        const val DEFAULT_JPEG_QUALITY = 0.6f
        const val DEFAULT_MAX_IMAGE_PX = 1600
        private const val COMPRESS_MIN_PX = 256
        private const val QUARTER_TURN_DEG = 90
        private const val FULL_TURN_DEG = 360
    }
}
