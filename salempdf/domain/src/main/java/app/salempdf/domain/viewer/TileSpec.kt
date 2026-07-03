package app.salempdf.domain.viewer

import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.min

/**
 * One hi-res tile of a page: coordinates in "bucket space" — the page
 * rasterized at zoom [bucket] (page width = viewportWidth * bucket).
 */
data class TileSpec(
    val pageIndex: Int,
    val bucket: Float,
    val pageWidthPx: Int,
    val pageHeightPx: Int,
    val leftPx: Int,
    val topPx: Int,
    val widthPx: Int,
    val heightPx: Int,
) {
    val cacheKey: String = "tile:$pageIndex:$bucket:$leftPx:$topPx"
}

const val TILE_SIZE_PX = 512

/**
 * Tiles of [pageIndex] intersecting the viewport, in bucket space. Content
 * space (the [layout]'s zoom) is converted with scale = bucket / zoom.
 */
@Suppress("LongParameterList")
fun visibleTiles(
    layout: DocumentLayout,
    pageIndex: Int,
    scrollY: Float,
    offsetX: Float,
    viewportWidthPx: Float,
    viewportHeightPx: Float,
    bucket: Float,
    tilePx: Int = TILE_SIZE_PX,
): List<TileSpec> {
    val toBucket = bucket / layout.zoom
    val pageTop = layout.pageTopPx(pageIndex)
    val pageHeight = layout.pageHeightPx(pageIndex)

    // Visible slice of the page in content space, relative to the page origin.
    val visLeft = (-offsetX).coerceAtLeast(0f)
    val visRight = (viewportWidthPx - offsetX).coerceAtMost(layout.pageWidthPx)
    val visTop = (scrollY - pageTop).coerceAtLeast(0f)
    val visBottom = (scrollY + viewportHeightPx - pageTop).coerceAtMost(pageHeight)
    if (visLeft >= visRight || visTop >= visBottom) return emptyList()

    val pageWidthB = ceil(layout.pageWidthPx * toBucket).toInt()
    val pageHeightB = ceil(pageHeight * toBucket).toInt()
    val firstCol = floor(visLeft * toBucket / tilePx).toInt()
    val lastCol = floor((visRight * toBucket - 1f) / tilePx).toInt()
    val firstRow = floor(visTop * toBucket / tilePx).toInt()
    val lastRow = floor((visBottom * toBucket - 1f) / tilePx).toInt()

    val specs = mutableListOf<TileSpec>()
    for (row in firstRow..lastRow) {
        for (col in firstCol..lastCol) {
            val left = col * tilePx
            val top = row * tilePx
            val width = min(tilePx, pageWidthB - left)
            val height = min(tilePx, pageHeightB - top)
            if (width <= 0 || height <= 0) continue
            specs +=
                TileSpec(
                    pageIndex = pageIndex,
                    bucket = bucket,
                    pageWidthPx = pageWidthB,
                    pageHeightPx = pageHeightB,
                    leftPx = left,
                    topPx = top,
                    widthPx = width,
                    heightPx = height,
                )
        }
    }
    return specs
}
