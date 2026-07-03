package app.salempdf.viewer

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.drag
import androidx.compose.runtime.MutableState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.unit.dp
import app.salempdf.domain.annotation.PdfAnnotation
import app.salempdf.domain.annotation.PointPt
import app.salempdf.domain.render.PageText
import app.salempdf.domain.viewer.DocumentLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private const val MARKUP_TOLERANCE_PT = 14f
private const val MOVE_HIT_SLOP_PT = 6f

/**
 * Single-finger gestures for the active tool. Installed via
 * pointerInput(activeTool) BEFORE the transform handler, so consumed drawing
 * strokes never pan the page; two-finger gestures stay with the transform
 * handler.
 */
suspend fun PointerInputScope.toolGestures(
    viewModel: ViewerViewModel,
    transform: ViewerTransform,
    layoutProvider: () -> DocumentLayout,
    preview: MutableState<ToolPreview?>,
    onPlacement: (PlacementRequest) -> Unit,
    scope: CoroutineScope,
) {
    val tool = viewModel.activeTool ?: return selectAndMoveGestures(viewModel, transform, layoutProvider)
    when {
        tool.markupKind != null -> markupDragGestures(viewModel, transform, layoutProvider, scope, tool)
        tool == AnnotationTool.INK -> inkDragGestures(viewModel, transform, layoutProvider, preview)
        tool.shapeKind != null -> shapeDragGestures(viewModel, transform, layoutProvider, preview, tool)
        else -> tapPlacementGestures(transform, layoutProvider, tool, onPlacement)
    }
}

private suspend fun PointerInputScope.tapPlacementGestures(
    transform: ViewerTransform,
    layoutProvider: () -> DocumentLayout,
    tool: AnnotationTool,
    onPlacement: (PlacementRequest) -> Unit,
) {
    detectTapGestures { position ->
        val hit = screenToPagePoint(position, layoutProvider(), transform) ?: return@detectTapGestures
        onPlacement(PlacementRequest(tool, hit.first, PointPt(hit.second.x, hit.second.y)))
    }
}

private suspend fun PointerInputScope.inkDragGestures(
    viewModel: ViewerViewModel,
    transform: ViewerTransform,
    layoutProvider: () -> DocumentLayout,
    preview: MutableState<ToolPreview?>,
) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        val layout = layoutProvider()
        val hit = screenToPagePoint(down.position, layout, transform) ?: return@awaitEachGesture
        val pageIndex = hit.first
        down.consume()
        val points = mutableListOf(PointPt(hit.second.x, hit.second.y))
        val color = viewModel.toolColor
        val width = viewModel.toolStrokeWidthPt
        preview.value = ToolPreview.InkStroke(pageIndex, points.toList(), color, width)
        drag(down.id) { change ->
            change.consume()
            val point = screenToPagePoint(change.position, layoutProvider(), transform)
            if (point != null && point.first == pageIndex) {
                points += PointPt(point.second.x, point.second.y)
                preview.value = ToolPreview.InkStroke(pageIndex, points.toList(), color, width)
            }
        }
        preview.value = null
        if (points.size > 1) {
            viewModel.addAnnotation(
                PdfAnnotation.Ink(
                    id = viewModel.newAnnotationId(),
                    pageIndex = pageIndex,
                    colorRgb = color,
                    opacity = 1f,
                    strokes = listOf(points.toList()),
                    strokeWidthPt = width,
                ),
            )
        }
    }
}

private suspend fun PointerInputScope.shapeDragGestures(
    viewModel: ViewerViewModel,
    transform: ViewerTransform,
    layoutProvider: () -> DocumentLayout,
    preview: MutableState<ToolPreview?>,
    tool: AnnotationTool,
) {
    val kind = tool.shapeKind ?: return
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        val layout = layoutProvider()
        val hit = screenToPagePoint(down.position, layout, transform) ?: return@awaitEachGesture
        val pageIndex = hit.first
        down.consume()
        val start = PointPt(hit.second.x, hit.second.y)
        var end = start
        val color = viewModel.toolColor
        val width = viewModel.toolStrokeWidthPt
        drag(down.id) { change ->
            change.consume()
            val point = screenToPagePoint(change.position, layoutProvider(), transform)
            if (point != null && point.first == pageIndex) {
                end = PointPt(point.second.x, point.second.y)
                preview.value = ToolPreview.ShapeDrag(pageIndex, kind, start, end, color, width)
            }
        }
        preview.value = null
        if (distanceSquared(start, end) > MIN_SHAPE_DRAG_PT2) {
            viewModel.addAnnotation(
                PdfAnnotation.Shape(
                    id = viewModel.newAnnotationId(),
                    pageIndex = pageIndex,
                    colorRgb = color,
                    opacity = 1f,
                    kind = kind,
                    start = start,
                    end = end,
                    strokeWidthPt = width,
                ),
            )
        }
    }
}

private const val MIN_SHAPE_DRAG_PT2 = 16f

private fun distanceSquared(
    a: PointPt,
    b: PointPt,
): Float {
    val dx = b.x - a.x
    val dy = b.y - a.y
    return dx * dx + dy * dy
}

/**
 * Markup tools reuse the text-selection machinery: dragging selects text
 * (drawn with the standard selection highlight); releasing converts the
 * selection into a markup annotation.
 */
private suspend fun PointerInputScope.markupDragGestures(
    viewModel: ViewerViewModel,
    transform: ViewerTransform,
    layoutProvider: () -> DocumentLayout,
    scope: CoroutineScope,
    tool: AnnotationTool,
) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        val layout = layoutProvider()
        val hit = screenToPagePoint(down.position, layout, transform) ?: return@awaitEachGesture
        val pageIndex = hit.first
        down.consume()
        var pageText: PageText? = null
        var anchor: IntRange = IntRange.EMPTY
        var range: IntRange = IntRange.EMPTY
        scope.launch {
            val text = viewModel.pageText(pageIndex)
            pageText = text
            val index = text.charIndexNear(hit.second.x, hit.second.y, MARKUP_TOLERANCE_PT)
            if (index != null) {
                anchor = text.wordRangeAt(index)
                range = anchor
                viewModel.setSelection(Selection(pageIndex, anchor))
            }
        }
        drag(down.id) { change ->
            change.consume()
            val text = pageText ?: return@drag
            if (anchor.isEmpty()) return@drag
            val point = screenToPagePoint(change.position, layoutProvider(), transform) ?: return@drag
            if (point.first != pageIndex) return@drag
            val index = text.charIndexNear(point.second.x, point.second.y, MARKUP_TOLERANCE_PT) ?: return@drag
            range =
                when {
                    index < anchor.first -> index..anchor.last
                    index > anchor.last -> anchor.first..index
                    else -> anchor
                }
            viewModel.setSelection(Selection(pageIndex, range))
        }
        val text = pageText
        if (text != null && !range.isEmpty()) {
            val rects = text.selectionRects(range)
            if (rects.isNotEmpty()) {
                val kind = tool.markupKind
                if (kind != null) {
                    viewModel.addAnnotation(
                        PdfAnnotation.TextMarkup(
                            id = viewModel.newAnnotationId(),
                            pageIndex = pageIndex,
                            colorRgb = viewModel.toolColor,
                            opacity = tool.defaultOpacity,
                            kind = kind,
                            lineRects = rects,
                        ),
                    )
                }
            }
        }
        viewModel.setSelection(null)
    }
}

/**
 * Pan/select mode: dragging that starts on the selected annotation moves it
 * (or resizes via the bottom-right handle); everything else is left to the
 * transform handler.
 */
private suspend fun PointerInputScope.selectAndMoveGestures(
    viewModel: ViewerViewModel,
    transform: ViewerTransform,
    layoutProvider: () -> DocumentLayout,
) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        val selected = viewModel.selectedAnnotation() ?: return@awaitEachGesture
        val layout = layoutProvider()
        val hit = screenToPagePoint(down.position, layout, transform) ?: return@awaitEachGesture
        if (hit.first != selected.pageIndex) return@awaitEachGesture
        val scale = layout.pageScale(selected.pageIndex)
        val handlePt = RESIZE_HANDLE_DP.dp.toPx() / scale
        val bounds = selected.bounds
        val nearHandle =
            hit.second.x >= bounds.right - handlePt && hit.second.x <= bounds.right + handlePt &&
                hit.second.y >= bounds.bottom - handlePt && hit.second.y <= bounds.bottom + handlePt
        val inside =
            hit.second.x >= bounds.left - MOVE_HIT_SLOP_PT && hit.second.x <= bounds.right + MOVE_HIT_SLOP_PT &&
                hit.second.y >= bounds.top - MOVE_HIT_SLOP_PT && hit.second.y <= bounds.bottom + MOVE_HIT_SLOP_PT
        if (!inside && !nearHandle) return@awaitEachGesture
        down.consume()
        var current = selected
        var last = hit.second
        drag(down.id) { change ->
            change.consume()
            val point = screenToPagePoint(change.position, layoutProvider(), transform) ?: return@drag
            if (point.first != selected.pageIndex) return@drag
            val dx = point.second.x - last.x
            val dy = point.second.y - last.y
            last = point.second
            current = if (nearHandle) resize(current, dx, dy) else current.movedBy(dx, dy)
            viewModel.replaceAnnotation(current)
        }
    }
}

/** Resizes the resizable types by dragging the bottom-right corner; others move instead. */
private fun resize(
    annotation: PdfAnnotation,
    dx: Float,
    dy: Float,
): PdfAnnotation =
    when (annotation) {
        is PdfAnnotation.Shape ->
            annotation.copy(
                end = PointPt(annotation.end.x + dx, annotation.end.y + dy),
            )
        is PdfAnnotation.FreeText ->
            annotation.copy(
                rect =
                    annotation.rect.let {
                        app.salempdf.domain.render.RectPt(
                            it.left,
                            it.top,
                            (it.right + dx).coerceAtLeast(it.left + MIN_RESIZE_PT),
                            (it.bottom + dy).coerceAtLeast(it.top + MIN_RESIZE_PT),
                        )
                    },
            )
        is PdfAnnotation.Stamp ->
            annotation.copy(
                rect =
                    annotation.rect.let {
                        app.salempdf.domain.render.RectPt(
                            it.left,
                            it.top,
                            (it.right + dx).coerceAtLeast(it.left + MIN_RESIZE_PT),
                            (it.bottom + dy).coerceAtLeast(it.top + MIN_RESIZE_PT),
                        )
                    },
            )
        else -> annotation.movedBy(dx, dy)
    }

private const val MIN_RESIZE_PT = 12f

/** Topmost annotation whose bounds contain the given page point. */
fun hitTestAnnotation(
    annotations: List<PdfAnnotation>,
    pageIndex: Int,
    point: Offset,
): PdfAnnotation? =
    annotations.lastOrNull { annotation ->
        annotation.pageIndex == pageIndex &&
            annotation.bounds.contains(point.x, point.y)
    }
