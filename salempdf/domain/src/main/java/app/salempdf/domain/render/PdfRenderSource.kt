package app.salempdf.domain.render

import android.graphics.Bitmap
import app.salempdf.domain.model.PageSize

/**
 * A region of one page to rasterize: the page is notionally laid out at
 * [pageWidthPx] x [pageHeightPx] and the rectangle at ([left], [top]) sized
 * [width] x [height] of that layout is rendered. With left/top = 0 and
 * width/height equal to the page layout size this renders the whole page;
 * anything smaller is a tile.
 */
data class RenderRegion(
    val pageIndex: Int,
    val pageWidthPx: Int,
    val pageHeightPx: Int,
    val left: Int = 0,
    val top: Int = 0,
    val width: Int = pageWidthPx,
    val height: Int = pageHeightPx,
) {
    init {
        require(pageWidthPx > 0 && pageHeightPx > 0) { "Page raster size must be positive" }
        require(width > 0 && height > 0) { "Region size must be positive" }
    }
}

/**
 * Read side of the engine seam (ARCHITECTURE.md): implemented over Pdfium in
 * :app. All functions are safe to call from any dispatcher; implementations
 * serialize engine access internally.
 */
interface PdfRenderSource : AutoCloseable {
    val pageCount: Int

    /** Size of the page in PDF points; cached after first load. */
    suspend fun pageSize(index: Int): PageSize

    /** Rasterizes [region] into a fresh bitmap. */
    suspend fun renderRegion(region: RenderRegion): Bitmap

    /** Extracted text and per-character boxes (top-left-origin points). */
    suspend fun pageText(index: Int): PageText
}
