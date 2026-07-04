package app.salempdf.viewer

import android.graphics.Paint
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import app.salempdf.domain.annotation.MarkupKind
import app.salempdf.domain.annotation.PdfAnnotation
import app.salempdf.domain.annotation.PointPt
import app.salempdf.domain.annotation.ShapeKind
import app.salempdf.domain.render.RectPt
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/** Screen-space placement of one page: page points * [scale] + ([left], [top]). */
data class PagePlacement(val left: Float, val top: Float, val scale: Float) {
    fun x(xPt: Float): Float = left + xPt * scale

    fun y(yPt: Float): Float = top + yPt * scale

    fun offset(point: PointPt): Offset = Offset(x(point.x), y(point.y))

    fun rect(rectPt: RectPt): Pair<Offset, Size> =
        Offset(x(rectPt.left), y(rectPt.top)) to Size(rectPt.width * scale, rectPt.height * scale)
}

/** In-progress tool gesture, drawn like the final annotation will look. */
sealed interface ToolPreview {
    val pageIndex: Int

    data class InkStroke(
        override val pageIndex: Int,
        val points: List<PointPt>,
        val colorRgb: Int,
        val widthPt: Float,
    ) : ToolPreview

    data class ShapeDrag(
        override val pageIndex: Int,
        val kind: ShapeKind,
        val start: PointPt,
        val end: PointPt,
        val colorRgb: Int,
        val widthPt: Float,
    ) : ToolPreview
}

private fun Int.toColor(alpha: Float = 1f): Color =
    Color(
        red = ((this shr 16) and 0xFF) / 255f,
        green = ((this shr 8) and 0xFF) / 255f,
        blue = (this and 0xFF) / 255f,
        alpha = alpha,
    )

fun DrawScope.drawAnnotation(
    annotation: PdfAnnotation,
    placement: PagePlacement,
    viewModel: ViewerViewModel,
) {
    when (annotation) {
        is PdfAnnotation.TextMarkup -> drawTextMarkup(annotation, placement)
        is PdfAnnotation.Ink -> drawInk(annotation, placement)
        is PdfAnnotation.Shape -> drawShape(annotation, placement)
        is PdfAnnotation.Note -> drawNote(annotation, placement)
        is PdfAnnotation.FreeText -> drawFreeText(annotation, placement)
        is PdfAnnotation.Stamp -> drawStamp(annotation, placement, viewModel)
    }
}

fun DrawScope.drawToolPreview(
    preview: ToolPreview,
    placement: PagePlacement,
) {
    when (preview) {
        is ToolPreview.InkStroke ->
            drawStrokePath(preview.points, placement, preview.colorRgb.toColor(), preview.widthPt * placement.scale)
        is ToolPreview.ShapeDrag ->
            drawShapeGeometry(
                kind = preview.kind,
                start = preview.start,
                end = preview.end,
                placement = placement,
                color = preview.colorRgb.toColor(),
                strokeWidth = preview.widthPt * placement.scale,
            )
    }
}

fun DrawScope.drawSelectionBox(
    bounds: RectPt,
    placement: PagePlacement,
) {
    val (topLeft, size) = placement.rect(bounds)
    val pad = 4.dp.toPx()
    drawRect(
        color = Color(0xFF4F46E5),
        topLeft = topLeft - Offset(pad, pad),
        size = Size(size.width + pad * 2, size.height + pad * 2),
        style =
            Stroke(
                width = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f)),
            ),
    )
    // Resize handle, bottom-right.
    val handle = RESIZE_HANDLE_DP.dp.toPx()
    drawRect(
        color = Color(0xFF4F46E5),
        topLeft = Offset(topLeft.x + size.width + pad - handle / 2, topLeft.y + size.height + pad - handle / 2),
        size = Size(handle, handle),
    )
}

const val RESIZE_HANDLE_DP = 14

private fun DrawScope.drawTextMarkup(
    annotation: PdfAnnotation.TextMarkup,
    placement: PagePlacement,
) {
    val color = annotation.colorRgb.toColor(if (annotation.kind == MarkupKind.HIGHLIGHT) annotation.opacity else 1f)
    for (lineRect in annotation.lineRects) {
        val (topLeft, size) = placement.rect(lineRect)
        when (annotation.kind) {
            MarkupKind.HIGHLIGHT -> drawRect(color, topLeft, size)
            MarkupKind.UNDERLINE ->
                drawLine(
                    color = color,
                    start = Offset(topLeft.x, topLeft.y + size.height),
                    end = Offset(topLeft.x + size.width, topLeft.y + size.height),
                    strokeWidth = (size.height * 0.08f).coerceAtLeast(1.5f),
                )
            MarkupKind.STRIKEOUT ->
                drawLine(
                    color = color,
                    start = Offset(topLeft.x, topLeft.y + size.height * 0.55f),
                    end = Offset(topLeft.x + size.width, topLeft.y + size.height * 0.55f),
                    strokeWidth = (size.height * 0.08f).coerceAtLeast(1.5f),
                )
        }
    }
}

private fun DrawScope.drawInk(
    annotation: PdfAnnotation.Ink,
    placement: PagePlacement,
) {
    val color = annotation.colorRgb.toColor(annotation.opacity)
    for (stroke in annotation.strokes) {
        drawStrokePath(stroke, placement, color, annotation.strokeWidthPt * placement.scale)
    }
}

private fun DrawScope.drawStrokePath(
    points: List<PointPt>,
    placement: PagePlacement,
    color: Color,
    strokeWidth: Float,
) {
    if (points.isEmpty()) return
    if (points.size == 1) {
        drawCircle(color, radius = strokeWidth / 2, center = placement.offset(points[0]))
        return
    }
    val path = Path()
    val first = placement.offset(points[0])
    path.moveTo(first.x, first.y)
    for (i in 1 until points.size) {
        val p = placement.offset(points[i])
        path.lineTo(p.x, p.y)
    }
    drawPath(
        path = path,
        color = color,
        style = Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round),
    )
}

private fun DrawScope.drawShape(
    annotation: PdfAnnotation.Shape,
    placement: PagePlacement,
) {
    drawShapeGeometry(
        kind = annotation.kind,
        start = annotation.start,
        end = annotation.end,
        placement = placement,
        color = annotation.colorRgb.toColor(annotation.opacity),
        strokeWidth = annotation.strokeWidthPt * placement.scale,
    )
}

private fun DrawScope.drawShapeGeometry(
    kind: ShapeKind,
    start: PointPt,
    end: PointPt,
    placement: PagePlacement,
    color: Color,
    strokeWidth: Float,
) {
    val a = placement.offset(start)
    val b = placement.offset(end)
    when (kind) {
        ShapeKind.RECT ->
            drawRect(
                color = color,
                topLeft = Offset(minOf(a.x, b.x), minOf(a.y, b.y)),
                size = Size(kotlin.math.abs(b.x - a.x), kotlin.math.abs(b.y - a.y)),
                style = Stroke(strokeWidth),
            )
        ShapeKind.OVAL ->
            drawOval(
                color = color,
                topLeft = Offset(minOf(a.x, b.x), minOf(a.y, b.y)),
                size = Size(kotlin.math.abs(b.x - a.x), kotlin.math.abs(b.y - a.y)),
                style = Stroke(strokeWidth),
            )
        ShapeKind.LINE -> drawLine(color, a, b, strokeWidth)
        ShapeKind.ARROW -> {
            drawLine(color, a, b, strokeWidth)
            val angle = atan2(b.y - a.y, b.x - a.x)
            val headLength = (strokeWidth * 4).coerceAtLeast(16f)
            val spread = 0.5f
            drawLine(
                color = color,
                start = b,
                end =
                    Offset(
                        b.x - headLength * cos(angle - spread),
                        b.y - headLength * sin(angle - spread),
                    ),
                strokeWidth = strokeWidth,
            )
            drawLine(
                color = color,
                start = b,
                end =
                    Offset(
                        b.x - headLength * cos(angle + spread),
                        b.y - headLength * sin(angle + spread),
                    ),
                strokeWidth = strokeWidth,
            )
        }
    }
}

private fun DrawScope.drawNote(
    annotation: PdfAnnotation.Note,
    placement: PagePlacement,
) {
    val (topLeft, size) = placement.rect(annotation.bounds)
    val color = annotation.colorRgb.toColor()
    drawRoundRect(
        color = color,
        topLeft = topLeft,
        size = Size(size.width, size.height * 0.8f),
        cornerRadius = CornerRadius(size.width * 0.2f),
    )
    // Speech-bubble tail.
    val tail = Path()
    tail.moveTo(topLeft.x + size.width * 0.25f, topLeft.y + size.height * 0.75f)
    tail.lineTo(topLeft.x + size.width * 0.2f, topLeft.y + size.height)
    tail.lineTo(topLeft.x + size.width * 0.5f, topLeft.y + size.height * 0.8f)
    tail.close()
    drawPath(tail, color)
    val dotY = topLeft.y + size.height * 0.4f
    for (i in 0..2) {
        drawCircle(
            color = Color.White,
            radius = size.width * 0.06f,
            center = Offset(topLeft.x + size.width * (0.3f + i * 0.2f), dotY),
        )
    }
}

private fun DrawScope.drawFreeText(
    annotation: PdfAnnotation.FreeText,
    placement: PagePlacement,
) {
    val (topLeft, size) = placement.rect(annotation.rect)
    val paint =
        Paint().apply {
            isAntiAlias = true
            color = (0xFF000000.toInt() or annotation.colorRgb)
            textSize = annotation.fontSizePt * placement.scale
        }
    val lineHeight = paint.textSize * 1.25f
    var y = topLeft.y + paint.textSize
    // Simple greedy wrap within the box width.
    for (paragraph in annotation.text.split('\n')) {
        var line = StringBuilder()
        for (word in paragraph.split(' ')) {
            val candidate = if (line.isEmpty()) word else "$line $word"
            if (paint.measureText(candidate) > size.width && line.isNotEmpty()) {
                drawContext.canvas.nativeCanvas.drawText(line.toString(), topLeft.x, y, paint)
                y += lineHeight
                line = StringBuilder(word)
            } else {
                line = StringBuilder(candidate)
            }
        }
        drawContext.canvas.nativeCanvas.drawText(line.toString(), topLeft.x, y, paint)
        y += lineHeight
    }
}

private fun DrawScope.drawStamp(
    annotation: PdfAnnotation.Stamp,
    placement: PagePlacement,
    viewModel: ViewerViewModel,
) {
    val image = viewModel.renderer.stampImage(annotation) ?: return
    val (topLeft, size) = placement.rect(annotation.rect)
    drawImage(
        image = image,
        srcOffset = androidx.compose.ui.unit.IntOffset.Zero,
        srcSize = androidx.compose.ui.unit.IntSize(image.width, image.height),
        dstOffset = androidx.compose.ui.unit.IntOffset(topLeft.x.toInt(), topLeft.y.toInt()),
        dstSize = androidx.compose.ui.unit.IntSize(size.width.toInt(), size.height.toInt()),
    )
}
