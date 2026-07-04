package dev.salemlift.app.analytics

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.sp
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.core.cartesian.axis.Axis
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import kotlin.math.ceil
import kotlin.math.max

/** Titled y axis; when [maxY] is known, integer steps sized for ~6 labels. */
@Composable
internal fun startAxis(
    title: String,
    maxY: Double?,
): VerticalAxis<Axis.Position.Vertical.Start> =
    VerticalAxis.rememberStart(
        valueFormatter = intFormatter,
        itemPlacer =
            maxY?.let { top ->
                VerticalAxis.ItemPlacer.step({ max(1.0, ceil(top / TARGET_Y_LABELS)) })
            } ?: VerticalAxis.ItemPlacer.count(),
        title = title,
        titleComponent = axisTitleComponent(),
    )

/** Titled x axis with one label per data point. */
@Composable
internal fun bottomAxis(
    title: String,
    valueFormatter: CartesianValueFormatter,
): HorizontalAxis<Axis.Position.Horizontal.Bottom> =
    HorizontalAxis.rememberBottom(
        valueFormatter = valueFormatter,
        itemPlacer = HorizontalAxis.ItemPlacer.aligned(),
        title = title,
        titleComponent = axisTitleComponent(),
    )

@Composable
private fun axisTitleComponent() =
    rememberTextComponent(
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textSize = 11.sp,
    )

/** "W1", "W2", … labels for week-numbered x axes. */
@Composable
internal fun weekFormatter(): CartesianValueFormatter = remember { labelFormatter { "W${it.toInt()}" } }

internal val intFormatter: CartesianValueFormatter = labelFormatter { it.toLong().toString() }

internal fun labelFormatter(toLabel: (Double) -> String): CartesianValueFormatter =
    object : CartesianValueFormatter {
        override fun format(
            context: CartesianMeasuringContext,
            value: Double,
            verticalAxisPosition: Axis.Position.Vertical?,
        ): CharSequence = toLabel(value)
    }

private const val TARGET_Y_LABELS = 6.0
