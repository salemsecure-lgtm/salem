package app.salempdf.pdf

import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.net.Uri
import android.os.ParcelFileDescriptor
import app.salempdf.domain.model.PageSize
import app.salempdf.domain.render.PageText
import app.salempdf.domain.render.PdfRenderSource
import app.salempdf.domain.render.RectPt
import app.salempdf.domain.render.RenderRegion
import io.legere.pdfiumandroid.suspend.PdfDocumentKt
import io.legere.pdfiumandroid.suspend.PdfPageKt
import io.legere.pdfiumandroid.suspend.PdfiumCoreKt
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException
import java.util.concurrent.Executors

/**
 * [PdfRenderSource] over Pdfium. Pdfium is not thread-safe: the library
 * serializes native calls on [pdfiumDispatcher] (a single thread), and this
 * class serializes its own page-cache bookkeeping with a [Mutex].
 */
class PdfiumRenderSource private constructor(
    private val document: PdfDocumentKt,
    private val fileDescriptor: ParcelFileDescriptor,
    override val pageCount: Int,
) : PdfRenderSource {
    private val mutex = Mutex()
    private val pageCache = LinkedHashMap<Int, PdfPageKt>(MAX_OPEN_PAGES, 0.75f, true)
    private val sizeCache = HashMap<Int, PageSize>()
    private var closed = false

    override suspend fun pageSize(index: Int): PageSize =
        mutex.withLock {
            sizeCache[index] ?: run {
                val page = openPageLocked(index)
                val size = PageSize(page.getPageWidthPoint().toFloat(), page.getPageHeightPoint().toFloat())
                sizeCache[index] = size
                size
            }
        }

    override suspend fun renderRegion(region: RenderRegion): Bitmap =
        mutex.withLock {
            val page = openPageLocked(region.pageIndex)
            val bitmap = Bitmap.createBitmap(region.width, region.height, Bitmap.Config.ARGB_8888)
            page.renderPageBitmap(
                bitmap = bitmap,
                startX = -region.left,
                startY = -region.top,
                drawSizeX = region.pageWidthPx,
                drawSizeY = region.pageHeightPx,
                renderAnnot = true,
            )
            bitmap
        }

    override suspend fun pageText(index: Int): PageText =
        mutex.withLock {
            val page = openPageLocked(index)
            val heightPt = sizeCacheLockedHeight(index, page)
            page.openTextPage().use { textPage ->
                val count = textPage.textPageCountChars()
                if (count <= 0) return@use PageText.EMPTY
                val text = textPage.textPageGetText(0, count).orEmpty()
                val boxes =
                    (0 until count).map { i ->
                        textPage.textPageGetCharBox(i)?.toTopLeftRectPt(heightPt) ?: RectPt.EMPTY
                    }
                PageText(text, boxes)
            }
        }

    override fun close() {
        closed = true
        pageCache.values.forEach { runCatching { it.close() } }
        pageCache.clear()
        runCatching { document.close() }
        runCatching { fileDescriptor.close() }
    }

    private suspend fun openPageLocked(index: Int): PdfPageKt {
        check(!closed) { "Render source is closed" }
        pageCache[index]?.let { return it }
        val page = document.openPage(index) ?: throw IOException("Could not open page ${index + 1}")
        pageCache[index] = page
        if (pageCache.size > MAX_OPEN_PAGES) {
            val eldest = pageCache.entries.iterator()
            val victim = eldest.next()
            eldest.remove()
            runCatching { victim.value.close() }
        }
        return page
    }

    private suspend fun sizeCacheLockedHeight(
        index: Int,
        page: PdfPageKt,
    ): Float {
        sizeCache[index]?.let { return it.heightPt }
        val size = PageSize(page.getPageWidthPoint().toFloat(), page.getPageHeightPoint().toFloat())
        sizeCache[index] = size
        return size.heightPt
    }

    companion object {
        private const val MAX_OPEN_PAGES = 6

        // One shared thread for all Pdfium work in the process.
        private val pdfiumDispatcher = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
        private val core by lazy { PdfiumCoreKt(pdfiumDispatcher) }

        suspend fun open(
            context: Context,
            uri: Uri,
        ): PdfiumRenderSource {
            val descriptor =
                context.contentResolver.openFileDescriptor(uri, "r")
                    ?: throw IOException("Could not open document")
            try {
                val document = core.newDocument(descriptor)
                return PdfiumRenderSource(document, descriptor, document.getPageCount())
            } catch (
                // Pdfium's JNI layer surfaces corrupt/encrypted files as assorted runtime types.
                @Suppress("TooGenericExceptionCaught") e: Exception,
            ) {
                runCatching { descriptor.close() }
                throw IOException("Could not open this PDF", e)
            }
        }
    }
}

/**
 * Pdfium char boxes are in PDF user space (bottom-left origin, y up). Domain
 * space is top-left origin, so flip y around the page height. Coordinates are
 * normalized with min/max because Pdfium reports top/bottom in y-up terms.
 */
private fun RectF.toTopLeftRectPt(pageHeightPt: Float): RectPt {
    val yLow = minOf(top, bottom)
    val yHigh = maxOf(top, bottom)
    return RectPt(
        left = minOf(left, right),
        top = pageHeightPt - yHigh,
        right = maxOf(left, right),
        bottom = pageHeightPt - yLow,
    )
}
