package app.salempdf.viewer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import app.salempdf.domain.viewer.DocumentLayout
import app.salempdf.domain.viewer.ZoomBuckets

/**
 * Viewer camera: [zoom] (1 = fit width), [scrollY] and [offsetX] in content
 * pixels at the current zoom. Content coordinates scale linearly with zoom,
 * which keeps focal-point math a plain proportion.
 */
class ViewerTransform {
    var zoom by mutableFloatStateOf(1f)
        private set
    var scrollY by mutableFloatStateOf(0f)
        private set
    var offsetX by mutableFloatStateOf(0f)
        private set

    /** Applies a pinch/pan step keeping the content under [centroid] stationary. */
    fun applyGesture(
        centroid: Offset,
        pan: Offset,
        zoomChange: Float,
        layout: DocumentLayout,
        viewport: Size,
    ) {
        if (zoomChange != 1f) {
            val newZoom = (zoom * zoomChange).coerceIn(ZoomBuckets.MIN_ZOOM, ZoomBuckets.MAX_ZOOM)
            rescaleAbout(centroid, newZoom)
        }
        scrollY -= pan.y
        offsetX += pan.x
        clamp(layout, viewport)
    }

    /** Sets zoom directly (double-tap / animations), anchored at [focal]. */
    fun setZoomFocal(
        newZoom: Float,
        focal: Offset,
        layout: DocumentLayout,
        viewport: Size,
    ) {
        rescaleAbout(focal, newZoom.coerceIn(ZoomBuckets.MIN_ZOOM, ZoomBuckets.MAX_ZOOM))
        clamp(layout, viewport)
    }

    fun scrollBy(
        dy: Float,
        layout: DocumentLayout,
        viewport: Size,
    ) {
        scrollY += dy
        clamp(layout, viewport)
    }

    fun scrollTo(
        y: Float,
        layout: DocumentLayout,
        viewport: Size,
    ) {
        scrollY = y
        clamp(layout, viewport)
    }

    /**
     * Note: callers pass a [layout] built at the *current* [zoom]; content
     * heights scale linearly with zoom so clamping right after a zoom change
     * uses totalHeight scaled by zoom / layout.zoom.
     */
    fun clamp(
        layout: DocumentLayout,
        viewport: Size,
    ) {
        val scaleFix = if (layout.zoom > 0f) zoom / layout.zoom else 1f
        val total = layout.totalHeightPx * scaleFix
        val maxY = (total - viewport.height).coerceAtLeast(0f)
        scrollY = scrollY.coerceIn(0f, maxY)
        val pageWidth = layout.pageWidthPx * scaleFix
        offsetX =
            if (pageWidth <= viewport.width) {
                (viewport.width - pageWidth) / 2f
            } else {
                offsetX.coerceIn(viewport.width - pageWidth, 0f)
            }
    }

    private fun rescaleAbout(
        focal: Offset,
        newZoom: Float,
    ) {
        val k = newZoom / zoom
        scrollY = k * (scrollY + focal.y) - focal.y
        offsetX = focal.x - k * (focal.x - offsetX)
        zoom = newZoom
    }
}

/** Maps a screen position to (page index, point in top-left-origin page points), or null off-page. */
fun screenToPagePoint(
    position: Offset,
    layout: DocumentLayout,
    transform: ViewerTransform,
): Pair<Int, Offset>? {
    if (layout.pageCount == 0) return null
    val contentY = position.y + transform.scrollY
    val page = layout.pageAtY(contentY)
    val top = layout.pageTopPx(page)
    if (contentY < top || contentY > top + layout.pageHeightPx(page)) return null
    val scale = layout.pageScale(page)
    val xPt = (position.x - transform.offsetX) / scale
    val yPt = (contentY - top) / scale
    val size = layout.pageSizePt(page)
    if (xPt < 0f || xPt > size.widthPt) return null
    return page to Offset(xPt, yPt)
}
