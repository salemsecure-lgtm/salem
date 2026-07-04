package dev.salemlift.app.analytics

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.layer.continuous
import com.patrykandpatrick.vico.compose.cartesian.layer.point
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.compose.common.shape.rounded
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.decoration.HorizontalLine
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.patrykandpatrick.vico.core.common.Position
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import dev.salemlift.data.analytics.E1rmPoint
import dev.salemlift.data.analytics.WeekTonnage
import dev.salemlift.data.analytics.WeekVolume
import dev.salemlift.domain.model.Landmarks
import kotlin.math.max

/**
 * Volume tab chart: performed weekly hard sets (columns) with the prescribed
 * sets overlaid (line) and the muscle's MV/MEV/MAV/MRV drawn as labeled
 * reference lines. Fits 384dp with scroll disabled — max 6 week columns.
 */
@Composable
fun VolumeChart(
    weeks: List<WeekVolume>,
    landmarks: Landmarks?,
    modifier: Modifier = Modifier,
) {
    val producer = remember { CartesianChartModelProducer() }
    LaunchedEffect(weeks) {
        producer.runTransaction {
            columnSeries { series(weeks.map { it.week }, weeks.map { it.performedSets }) }
            lineSeries { series(weeks.map { it.week }, weeks.map { it.prescribedSets }) }
        }
    }
    val palette = chartPalette()
    val dataMax = weeks.maxOfOrNull { max(it.performedSets, it.prescribedSets) } ?: 0
    val maxY = max(dataMax, landmarks?.mrv ?: 0) + 1.0
    val range = CartesianLayerRangeProvider.fixed(minY = 0.0, maxY = maxY)
    CartesianChartHost(
        chart =
            rememberCartesianChart(
                columnLayer(color = palette.primary, rangeProvider = range),
                rememberLineCartesianLayer(
                    lineProvider =
                        LineCartesianLayer.LineProvider.series(
                            trendLine(color = palette.secondary, pointSize = 6.dp),
                        ),
                    rangeProvider = range,
                ),
                startAxis = startAxis(title = "Hard sets", maxY = maxY),
                bottomAxis = bottomAxis(title = "Week", valueFormatter = weekFormatter()),
                decorations = landmarkLines(landmarks),
            ),
        modelProducer = producer,
        scrollState = rememberVicoScrollState(scrollEnabled = false),
        modifier = modifier.fillMaxWidth().height(CHART_HEIGHT),
    )
}

/** Strength tab: best-set e1RM per completed session, x labeled "W1·D2" etc. */
@Composable
fun E1rmChart(
    points: List<E1rmPoint>,
    modifier: Modifier = Modifier,
) {
    val producer = remember { CartesianChartModelProducer() }
    LaunchedEffect(points) {
        producer.runTransaction {
            lineSeries { series(points.indices.toList(), points.map { it.e1rmKg }) }
        }
    }
    val palette = chartPalette()
    val labels = points.map { "W${it.week}·D${it.dayIndex + 1}" }
    val sessionFormatter = remember(labels) { labelFormatter { labels.getOrNull(it.toInt()) ?: "" } }
    CartesianChartHost(
        chart =
            rememberCartesianChart(
                rememberLineCartesianLayer(
                    lineProvider =
                        LineCartesianLayer.LineProvider.series(
                            trendLine(color = palette.primary, pointSize = 8.dp),
                        ),
                ),
                startAxis = startAxis(title = "e1RM (kg)", maxY = null),
                bottomAxis = bottomAxis(title = "Session", valueFormatter = sessionFormatter),
            ),
        modelProducer = producer,
        scrollState = rememberVicoScrollState(scrollEnabled = false),
        modifier = modifier.fillMaxWidth().height(CHART_HEIGHT),
    )
}

/** Strength tab: weekly tonnage (Σ weight × reps) columns. */
@Composable
fun TonnageChart(
    weeks: List<WeekTonnage>,
    modifier: Modifier = Modifier,
) {
    val producer = remember { CartesianChartModelProducer() }
    LaunchedEffect(weeks) {
        producer.runTransaction {
            columnSeries { series(weeks.map { it.week }, weeks.map { it.tonnageKg }) }
        }
    }
    CartesianChartHost(
        chart =
            rememberCartesianChart(
                columnLayer(
                    color = chartPalette().primary,
                    rangeProvider = CartesianLayerRangeProvider.fixed(minY = 0.0),
                ),
                startAxis = startAxis(title = "Tonnage (kg)", maxY = null),
                bottomAxis = bottomAxis(title = "Week", valueFormatter = weekFormatter()),
            ),
        modelProducer = producer,
        scrollState = rememberVicoScrollState(scrollEnabled = false),
        modifier = modifier.fillMaxWidth().height(CHART_HEIGHT),
    )
}

/** Thin rounded-top columns anchored to the zero baseline. */
@Composable
private fun columnLayer(
    color: Color,
    rangeProvider: CartesianLayerRangeProvider,
): ColumnCartesianLayer =
    rememberColumnCartesianLayer(
        columnProvider =
            ColumnCartesianLayer.ColumnProvider.series(
                rememberLineComponent(
                    fill = fill(color),
                    thickness = 14.dp,
                    shape = CorneredShape.rounded(topLeft = 4.dp, topRight = 4.dp),
                ),
            ),
        rangeProvider = rangeProvider,
    )

/** 2dp line with round markers, per the mark specs. */
@Composable
private fun trendLine(
    color: Color,
    pointSize: androidx.compose.ui.unit.Dp,
): LineCartesianLayer.Line =
    LineCartesianLayer.rememberLine(
        fill = LineCartesianLayer.LineFill.single(fill(color)),
        stroke = LineCartesianLayer.LineStroke.continuous(thickness = 2.dp),
        pointProvider =
            LineCartesianLayer.PointProvider.single(
                LineCartesianLayer.point(
                    rememberShapeComponent(fill(color), CorneredShape.Pill),
                    size = pointSize,
                ),
            ),
    )

/** MV/MEV/MAV/MRV reference lines in recessive outline ink, labeled at the end. */
@Composable
private fun landmarkLines(landmarks: Landmarks?): List<HorizontalLine> {
    val line =
        rememberLineComponent(
            fill = fill(MaterialTheme.colorScheme.outline),
            thickness = 1.dp,
        )
    val label =
        rememberTextComponent(
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textSize = 10.sp,
        )
    val marks = landmarks ?: return emptyList()
    return listOf(
        "MV" to marks.mv,
        "MEV" to marks.mev,
        "MAV" to marks.mav,
        "MRV" to marks.mrv,
    ).map { (name, sets) ->
        HorizontalLine(
            y = { sets.toDouble() },
            line = line,
            labelComponent = label,
            label = { name },
            horizontalLabelPosition = Position.Horizontal.End,
            verticalLabelPosition = Position.Vertical.Top,
        )
    }
}

private val CHART_HEIGHT = 220.dp
