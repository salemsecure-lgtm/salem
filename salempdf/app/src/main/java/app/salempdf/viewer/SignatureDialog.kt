package app.salempdf.viewer

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.salempdf.R
import java.io.ByteArrayOutputStream

private const val STROKE_WIDTH_PX = 6f

/** Draw-your-signature dialog; produces a transparent PNG for stamp placement. */
@Composable
fun SignatureDialog(
    onDismiss: () -> Unit,
    onDone: (ByteArray) -> Unit,
) {
    var strokes by remember { mutableStateOf(listOf<List<Offset>>()) }
    var current by remember { mutableStateOf(listOf<Offset>()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.sign_title)) },
        text = {
            androidx.compose.foundation.Canvas(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { start -> current = listOf(start) },
                                onDrag = { change, _ ->
                                    change.consume()
                                    current = current + change.position
                                },
                                onDragEnd = {
                                    if (current.size > 1) strokes = strokes + listOf(current)
                                    current = emptyList()
                                },
                            )
                        },
            ) {
                for (stroke in strokes + listOf(current)) {
                    if (stroke.size < 2) continue
                    val path = Path()
                    path.moveTo(stroke[0].x, stroke[0].y)
                    for (i in 1 until stroke.size) path.lineTo(stroke[i].x, stroke[i].y)
                    drawPath(
                        path = path,
                        color = Color(0xFF1F2937),
                        style = Stroke(STROKE_WIDTH_PX, cap = StrokeCap.Round, join = StrokeJoin.Round),
                    )
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = { strokes = emptyList() }) { Text(stringResource(R.string.clear)) }
                TextButton(
                    enabled = strokes.isNotEmpty(),
                    onClick = { renderSignature(strokes)?.let(onDone) },
                ) {
                    Text(stringResource(R.string.done))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}

/** Rasterizes the strokes to a tightly-cropped transparent PNG. */
private fun renderSignature(strokes: List<List<Offset>>): ByteArray? {
    val points = strokes.flatten()
    if (points.isEmpty()) return null
    val pad = STROKE_WIDTH_PX * 2
    val minX = points.minOf { it.x } - pad
    val minY = points.minOf { it.y } - pad
    val maxX = points.maxOf { it.x } + pad
    val maxY = points.maxOf { it.y } + pad
    val width = (maxX - minX).toInt().coerceAtLeast(1)
    val height = (maxY - minY).toInt().coerceAtLeast(1)
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint =
        Paint().apply {
            isAntiAlias = true
            color = android.graphics.Color.rgb(31, 41, 55)
            style = Paint.Style.STROKE
            strokeWidth = STROKE_WIDTH_PX
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
    val path = android.graphics.Path()
    for (stroke in strokes) {
        if (stroke.size < 2) continue
        path.moveTo(stroke[0].x - minX, stroke[0].y - minY)
        for (i in 1 until stroke.size) path.lineTo(stroke[i].x - minX, stroke[i].y - minY)
    }
    canvas.drawPath(path, paint)
    val out = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
    return out.toByteArray()
}
