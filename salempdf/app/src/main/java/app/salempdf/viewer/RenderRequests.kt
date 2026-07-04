package app.salempdf.viewer

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import app.salempdf.domain.annotation.PdfAnnotation
import app.salempdf.domain.model.PageSize
import app.salempdf.domain.render.PdfRenderSource
import app.salempdf.domain.render.RenderRegion
import app.salempdf.domain.viewer.TileSpec
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Bitmap provisioning for the viewer: base pages, hi-res tiles, thumbnails,
 * and stamp images, each deduplicated in-flight and LRU-bounded. Owned by
 * [ViewerViewModel]; draw code reads the caches (snapshot-backed, so a landed
 * render redraws the canvas).
 */
class RenderRequests(
    private val scope: CoroutineScope,
    private val sourceProvider: () -> PdfRenderSource?,
    private val pageSizes: SnapshotStateList<PageSize?>,
) {
    val baseCache = BitmapCache(BASE_CACHE_BYTES)
    val tileCache = BitmapCache(TILE_CACHE_BYTES)
    val thumbCache = BitmapCache(THUMB_CACHE_BYTES)

    private val inFlight = mutableSetOf<String>()
    private val stampImageCache = HashMap<String, ImageBitmap>()

    fun requestBase(
        pageIndex: Int,
        widthPx: Int,
    ) {
        val src = sourceProvider() ?: return
        val size = pageSizes.getOrNull(pageIndex) ?: return
        val heightPx = (widthPx / size.aspectRatio).toInt().coerceAtLeast(1)
        render(baseCache, "base:$pageIndex:$widthPx", src, RenderRegion(pageIndex, widthPx, heightPx))
    }

    fun requestTile(spec: TileSpec) {
        val src = sourceProvider() ?: return
        if (pageSizes.getOrNull(spec.pageIndex) == null) return
        render(
            cache = tileCache,
            key = spec.cacheKey,
            src = src,
            region =
                RenderRegion(
                    pageIndex = spec.pageIndex,
                    pageWidthPx = spec.pageWidthPx,
                    pageHeightPx = spec.pageHeightPx,
                    left = spec.leftPx,
                    top = spec.topPx,
                    width = spec.widthPx,
                    height = spec.heightPx,
                ),
        )
    }

    fun requestThumbnail(pageIndex: Int) {
        val src = sourceProvider() ?: return
        val size = pageSizes.getOrNull(pageIndex) ?: return
        val heightPx = (THUMB_WIDTH_PX / size.aspectRatio).toInt().coerceAtLeast(1)
        render(thumbCache, thumbKey(pageIndex), src, RenderRegion(pageIndex, THUMB_WIDTH_PX, heightPx))
    }

    fun thumbKey(pageIndex: Int): String = "thumb:$pageIndex"

    fun stampImage(annotation: PdfAnnotation.Stamp): ImageBitmap? =
        stampImageCache.getOrPut(annotation.id) {
            val bytes = annotation.pngBytes
            val decoded: Bitmap =
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null
            decoded.asImageBitmap()
        }

    fun clearAll() {
        baseCache.clear()
        tileCache.clear()
        thumbCache.clear()
        stampImageCache.clear()
    }

    private fun render(
        cache: BitmapCache,
        key: String,
        src: PdfRenderSource,
        region: RenderRegion,
    ) {
        if (cache.contains(key) || !inFlight.add(key)) return
        scope.launch {
            try {
                cache.put(key, src.renderRegion(region))
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (
                // Pdfium surfaces corrupt-page failures as assorted runtime types;
                // a failed tile just stays blank and the next scroll retries it.
                @Suppress("TooGenericExceptionCaught") ignored: Exception,
            ) {
                // Intentionally ignored.
            } finally {
                inFlight.remove(key)
            }
        }
    }

    companion object {
        private const val BASE_CACHE_BYTES = 48L * 1024 * 1024
        private const val TILE_CACHE_BYTES = 64L * 1024 * 1024
        private const val THUMB_CACHE_BYTES = 10L * 1024 * 1024
        private const val THUMB_WIDTH_PX = 144
    }
}
