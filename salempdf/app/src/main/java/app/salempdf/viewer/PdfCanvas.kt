package app.salempdf.viewer

import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.animateTo
import androidx.compose.animation.splineBasedDecay
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEach
import app.salempdf.domain.render.PageText
import app.salempdf.domain.render.RectPt
import app.salempdf.domain.viewer.DocumentLayout
import app.salempdf.domain.viewer.ZoomBuckets
import app.salempdf.domain.viewer.visibleTiles
import app.salempdf.ui.theme.Amber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

private const val DOUBLE_TAP_ZOOM = 2.5f
private const val TILE_ZOOM_THRESHOLD = 1.05f
private const val MIN_FLING_VELOCITY = 120f
private const val SELECT_TOLERANCE_PT = 14f
private const val PREFETCH_PAGES = 2

/** Tracks an in-progress long-press text selection drag. */
private class SelectionDrag {
    var pageIndex: Int = -1
    var pageText: PageText? = null
    var anchor: IntRange = IntRange.EMPTY
}

@Composable
fun PdfCanvas(
    viewModel: ViewerViewModel,
    transform: ViewerTransform,
    layoutProvider: () -> DocumentLayout,
    viewport: Size,
    onPlacement: (PlacementRequest) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val flingJob = remember { mutableStateOf<Job?>(null) }
    val toolPreview = remember { mutableStateOf<ToolPreview?>(null) }
    var selectionRects by remember { mutableStateOf<Pair<Int, List<RectPt>>?>(null) }
    val selection by viewModel.selection.collectAsState()

    RenderRequests(viewModel, transform, layoutProvider, viewport)

    LaunchedEffect(selection) {
        selectionRects =
            selection?.let { sel ->
                sel.pageIndex to viewModel.pageText(sel.pageIndex).selectionRects(sel.range)
            }
    }

    fun tapSelect(position: Offset) {
        val hit = screenToPagePoint(position, layoutProvider(), transform)
        val annotation =
            hit?.let { hitTestAnnotation(viewModel.annotations, it.first, Offset(it.second.x, it.second.y)) }
        viewModel.selectAnnotation(annotation?.id)
        if (annotation == null) viewModel.setSelection(null)
    }

    // Pointer dispatch note: the Main pass runs inner (later) modifiers first,
    // so the tool handler is LAST — its consumed single-finger gestures never
    // reach the transform handler, while two-finger pinch/pan still does.
    Canvas(
        modifier =
            modifier
                .pointerInput(Unit) {
                    transformAndFlingGestures(transform, layoutProvider, { viewportSize() }, scope, flingJob)
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = ::tapSelect,
                        onDoubleTap = { position ->
                            scope.launch {
                                animateDoubleTapZoom(transform, layoutProvider, viewportSize(), position)
                            }
                        },
                    )
                }
                .pointerInput(viewModel.activeTool) {
                    if (viewModel.activeTool == null) {
                        selectionGestures(viewModel, transform, layoutProvider, scope)
                    }
                }
                .pointerInput(viewModel.activeTool) {
                    toolGestures(viewModel, transform, layoutProvider, toolPreview, onPlacement, scope)
                },
    ) {
        drawDocument(viewModel, transform, layoutProvider(), viewport, selectionRects, toolPreview.value)
    }
}

private fun PointerInputScope.viewportSize(): Size = Size(size.width.toFloat(), size.height.toFloat())

@Composable
private fun RenderRequests(
    viewModel: ViewerViewModel,
    transform: ViewerTransform,
    layoutProvider: () -> DocumentLayout,
    viewport: Size,
) {
    val layout = layoutProvider()
    val baseWidth = layout.viewportWidthPx.roundToInt()
    val visible = layout.visiblePages(transform.scrollY, viewport.height)
    val bucket = ZoomBuckets.bucketFor(layout.zoom)
    val tileSpecs =
        if (layout.zoom > TILE_ZOOM_THRESHOLD) {
            visible.flatMap { page ->
                visibleTiles(
                    layout = layout,
                    pageIndex = page,
                    scrollY = transform.scrollY,
                    offsetX = transform.offsetX,
                    viewportWidthPx = viewport.width,
                    viewportHeightPx = viewport.height,
                    bucket = bucket,
                )
            }
        } else {
            emptyList()
        }

    LaunchedEffect(visible, baseWidth, viewModel.pageSizes.size) {
        if (visible.isEmpty()) return@LaunchedEffect
        val first = (visible.first - PREFETCH_PAGES).coerceAtLeast(0)
        val last = (visible.last + PREFETCH_PAGES).coerceAtMost(layout.pageCount - 1)
        for (page in first..last) viewModel.renderer.requestBase(page, baseWidth)
    }
    LaunchedEffect(tileSpecs) {
        tileSpecs.forEach(viewModel.renderer::requestTile)
    }
}

private suspend fun PointerInputScope.transformAndFlingGestures(
    transform: ViewerTransform,
    layoutProvider: () -> DocumentLayout,
    viewportProvider: () -> Size,
    scope: CoroutineScope,
    flingJob: MutableState<Job?>,
) {
    awaitEachGesture {
        awaitFirstDown(requireUnconsumed = false)
        flingJob.value?.cancel()
        val tracker = VelocityTracker()
        while (true) {
            val event = awaitPointerEvent()
            if (!event.changes.fastAny { it.pressed }) break
            if (event.changes.fastAny { it.isConsumed }) {
                tracker.resetTracking()
                continue
            }
            val zoomChange = event.calculateZoom()
            val pan = event.calculatePan()
            val centroid = event.calculateCentroid()
            if (zoomChange != 1f || pan != Offset.Zero) {
                transform.applyGesture(centroid, pan, zoomChange, layoutProvider(), viewportProvider())
                event.changes.fastForEach { it.consume() }
            }
            if (centroid != Offset.Unspecified) {
                tracker.addPosition(event.changes.first().uptimeMillis, centroid)
            }
        }
        startFling(tracker, transform, layoutProvider, viewportProvider, scope, flingJob)
    }
}

private fun PointerInputScope.startFling(
    tracker: VelocityTracker,
    transform: ViewerTransform,
    layoutProvider: () -> DocumentLayout,
    viewportProvider: () -> Size,
    scope: CoroutineScope,
    flingJob: MutableState<Job?>,
) {
    val velocityY = tracker.calculateVelocity().y
    if (abs(velocityY) < MIN_FLING_VELOCITY) return
    val decay = splineBasedDecay<Float>(this)
    flingJob.value =
        scope.launch {
            var last = 0f
            AnimationState(initialValue = 0f, initialVelocity = velocityY).animateDecay(decay) {
                transform.scrollBy(last - value, layoutProvider(), viewportProvider())
                last = value
            }
        }
}

private suspend fun animateDoubleTapZoom(
    transform: ViewerTransform,
    layoutProvider: () -> DocumentLayout,
    viewport: Size,
    focal: Offset,
) {
    val target = if (transform.zoom > 1.01f) 1f else DOUBLE_TAP_ZOOM
    AnimationState(initialValue = transform.zoom).animateTo(target) {
        transform.setZoomFocal(value, focal, layoutProvider(), viewport)
    }
}

private suspend fun PointerInputScope.selectionGestures(
    viewModel: ViewerViewModel,
    transform: ViewerTransform,
    layoutProvider: () -> DocumentLayout,
    scope: CoroutineScope,
) {
    val drag = SelectionDrag()

    fun startSelection(position: Offset) {
        drag.pageText = null
        val hit = screenToPagePoint(position, layoutProvider(), transform) ?: return
        scope.launch {
            val text = viewModel.pageText(hit.first)
            val index = text.charIndexNear(hit.second.x, hit.second.y, SELECT_TOLERANCE_PT) ?: return@launch
            drag.pageIndex = hit.first
            drag.pageText = text
            drag.anchor = text.wordRangeAt(index)
            viewModel.setSelection(Selection(hit.first, drag.anchor))
        }
    }

    fun extendSelection(position: Offset) {
        val text = drag.pageText ?: return
        val hit = screenToPagePoint(position, layoutProvider(), transform) ?: return
        if (hit.first != drag.pageIndex) return
        val index = text.charIndexNear(hit.second.x, hit.second.y, SELECT_TOLERANCE_PT) ?: return
        val range =
            when {
                index < drag.anchor.first -> index..drag.anchor.last
                index > drag.anchor.last -> drag.anchor.first..index
                else -> drag.anchor
            }
        viewModel.setSelection(Selection(drag.pageIndex, range))
    }

    detectDragGesturesAfterLongPress(
        onDragStart = ::startSelection,
        onDrag = { change, _ ->
            change.consume()
            extendSelection(change.position)
        },
    )
}

private fun DrawScope.drawDocument(
    viewModel: ViewerViewModel,
    transform: ViewerTransform,
    layout: DocumentLayout,
    viewport: Size,
    selectionRects: Pair<Int, List<RectPt>>?,
    toolPreview: ToolPreview?,
) {
    if (layout.pageCount == 0) return
    val visible = layout.visiblePages(transform.scrollY, viewport.height)
    val bucket = ZoomBuckets.bucketFor(layout.zoom)
    for (page in visible) {
        drawPage(viewModel, transform, layout, viewport, page, bucket)
        drawPageAnnotations(viewModel, transform, layout, page, toolPreview)
        if (selectionRects?.first == page) {
            drawSelection(transform, layout, page, selectionRects.second)
        }
    }
}

private fun DrawScope.drawPage(
    viewModel: ViewerViewModel,
    transform: ViewerTransform,
    layout: DocumentLayout,
    viewport: Size,
    page: Int,
    bucket: Float,
) {
    val left = transform.offsetX
    val top = layout.pageTopPx(page) - transform.scrollY
    val width = layout.pageWidthPx
    val height = layout.pageHeightPx(page)
    drawRect(Color.White, topLeft = Offset(left, top), size = Size(width, height))

    val baseKey = "base:$page:${layout.viewportWidthPx.roundToInt()}"
    viewModel.renderer.baseCache.get(baseKey)?.let { bitmap ->
        drawScaledBitmap(bitmap, left, top, width, height)
    }

    if (layout.zoom > TILE_ZOOM_THRESHOLD) {
        val fromBucket = layout.zoom / bucket
        val tiles =
            visibleTiles(
                layout = layout,
                pageIndex = page,
                scrollY = transform.scrollY,
                offsetX = transform.offsetX,
                viewportWidthPx = viewport.width,
                viewportHeightPx = viewport.height,
                bucket = bucket,
            )
        for (tile in tiles) {
            val bitmap = viewModel.renderer.tileCache.get(tile.cacheKey) ?: continue
            drawScaledBitmap(
                bitmap = bitmap,
                left = left + tile.leftPx * fromBucket,
                top = top + tile.topPx * fromBucket,
                width = tile.widthPx * fromBucket,
                height = tile.heightPx * fromBucket,
            )
        }
    }
}

private fun DrawScope.drawScaledBitmap(
    bitmap: android.graphics.Bitmap,
    left: Float,
    top: Float,
    width: Float,
    height: Float,
) {
    val dstLeft = left.roundToInt()
    val dstTop = top.roundToInt()
    drawImage(
        image = bitmap.asImageBitmap(),
        srcOffset = IntOffset.Zero,
        srcSize = IntSize(bitmap.width, bitmap.height),
        dstOffset = IntOffset(dstLeft, dstTop),
        dstSize = IntSize((left + width).roundToInt() - dstLeft, (top + height).roundToInt() - dstTop),
    )
}

private fun DrawScope.drawPageAnnotations(
    viewModel: ViewerViewModel,
    transform: ViewerTransform,
    layout: DocumentLayout,
    page: Int,
    toolPreview: ToolPreview?,
) {
    val placement =
        PagePlacement(
            left = transform.offsetX,
            top = layout.pageTopPx(page) - transform.scrollY,
            scale = layout.pageScale(page),
        )
    for (annotation in viewModel.annotations) {
        if (annotation.pageIndex != page) continue
        drawAnnotation(annotation, placement, viewModel)
    }
    if (toolPreview?.pageIndex == page) {
        drawToolPreview(toolPreview, placement)
    }
    val selected = viewModel.selectedAnnotation()
    if (selected != null && selected.pageIndex == page) {
        drawSelectionBox(selected.bounds, placement)
    }
}

private fun DrawScope.drawSelection(
    transform: ViewerTransform,
    layout: DocumentLayout,
    page: Int,
    rects: List<RectPt>,
) {
    val scale = layout.pageScale(page)
    val pageTop = layout.pageTopPx(page) - transform.scrollY
    for (rect in rects) {
        drawRect(
            color = Amber.copy(alpha = 0.4f),
            topLeft = Offset(transform.offsetX + rect.left * scale, pageTop + rect.top * scale),
            size = Size(rect.width * scale, rect.height * scale),
        )
    }
}
