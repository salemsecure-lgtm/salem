package app.salempdf.viewer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.salempdf.R

/** Tool chips + color palette + stroke widths + delete action for the selected annotation. */
@Composable
fun AnnotationToolbar(
    vm: ViewerViewModel,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
    ) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            items(AnnotationTool.entries) { tool ->
                FilterChip(
                    selected = vm.activeTool == tool,
                    onClick = { vm.setTool(tool) },
                    label = { Text(tool.label) },
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            AnnotationTool.PALETTE.forEach { color ->
                ColorSwatch(
                    color = color,
                    selected = vm.toolColor == color,
                    onClick = {
                        vm.toolColor = color
                        vm.applyColorToSelection(color)
                    },
                )
            }
            if (vm.activeTool.usesStroke()) {
                AnnotationTool.STROKE_WIDTHS_PT.forEach { width ->
                    StrokeSwatch(
                        widthPt = width,
                        selected = vm.toolStrokeWidthPt == width,
                        onClick = { vm.toolStrokeWidthPt = width },
                    )
                }
            }
            androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
            if (vm.selectedAnnotationId != null) {
                TextButton(onClick = vm::deleteSelectedAnnotation) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

private fun AnnotationTool?.usesStroke(): Boolean = this == AnnotationTool.INK || this?.shapeKind != null

@Composable
private fun ColorSwatch(
    color: Int,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outlineVariant
    Box(
        modifier =
            Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Color(0xFF000000.toInt() or color))
                .border(if (selected) 3.dp else 1.dp, borderColor, CircleShape)
                .clickable(onClick = onClick),
    )
}

@Composable
private fun StrokeSwatch(
    widthPt: Float,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(
                    if (selected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                )
                .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier =
                Modifier
                    .size((widthPt * 2.5f).dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface),
        )
    }
}
