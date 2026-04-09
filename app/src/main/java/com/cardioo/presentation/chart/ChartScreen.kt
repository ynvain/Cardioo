package com.cardioo.presentation.chart

import android.content.res.Configuration
import android.graphics.Paint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cardioo.R
import com.cardioo.domain.model.HealthMeasurement
import com.cardioo.domain.model.WeightUnit
import com.cardioo.domain.model.kgToPounds
import com.cardioo.domain.model.poundsToKg
import com.cardioo.presentation.theme.PinkAccent
import com.cardioo.presentation.theme.PinkPrimary
import com.cardioo.presentation.util.Orange
import com.cardioo.presentation.util.toggleButtonBorder
import com.cardioo.presentation.util.weightUnitString
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

@Composable
fun ChartScreen(
    contentPadding: PaddingValues,
    vm: ChartViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsState()
    val rangeFiltered = filterByRange(state.measurements, state.range)
    val chartData = when (state.metric) {
        ChartViewModel.Metric.Bp -> rangeFiltered
        ChartViewModel.Metric.Pulse -> rangeFiltered.filter { it.pulse != null }
        ChartViewModel.Metric.Weight -> rangeFiltered.filter { it.weight != null }
    }

    val periodLabel = stringResource(
        when (state.range) {
            ChartViewModel.Range.Week -> R.string.range_week
            ChartViewModel.Range.Month -> R.string.range_month
            ChartViewModel.Range.SixMonths -> R.string.range_six_months
            ChartViewModel.Range.Year -> R.string.range_year
            ChartViewModel.Range.AllTime -> R.string.range_all_time
        },
    )

    val weightDisplayUnit = chartData
        .filter { it.weight != null }
        .maxByOrNull { it.timestampEpochMillis }
        ?.weightUnit
        ?: WeightUnit.KG

    val yAxisLabel = when (state.metric) {
        ChartViewModel.Metric.Bp -> stringResource(R.string.chart_axis_mmhg)
        ChartViewModel.Metric.Pulse -> stringResource(R.string.chart_axis_bpm)
        ChartViewModel.Metric.Weight -> weightUnitString(weightDisplayUnit)
    }


    @Composable
    fun toggleMetricBorder(buttonMetric: ChartViewModel.Metric): BorderStroke {
        return toggleButtonBorder(state.metric == buttonMetric)
    }

    var rangeExpanded by remember { mutableStateOf(false) }
    var chartZoom by remember(state.metric, state.range) { mutableFloatStateOf(1f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(start = 2.dp, top = 5.dp, end = 5.dp, bottom = 5.dp)
            .pointerInput(state.metric, state.range) {
                detectTransformGestures { _, _, zoom, _ ->
                    chartZoom = (chartZoom * zoom).coerceIn(1f, 3f)
                }
            }
            .pointerInput(state.metric, state.range) {
                detectTapGestures(
                    onDoubleTap = { _ ->
                        if (chartZoom < 1.5f)
                            chartZoom = 1.5f
                        else
                            if (chartZoom < 3f)
                                chartZoom = 3f
                            else
                                if (chartZoom == 3f)
                                    chartZoom = 1f
                    }

                )
            },
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            OutlinedButton(
                onClick = { vm.setMetric(ChartViewModel.Metric.Bp) },
                border = toggleMetricBorder(ChartViewModel.Metric.Bp)
            ) {
                Text(
                    stringResource(
                        R.string.chart_metric_bp
                    ),
                )
            }
            OutlinedButton(
                onClick = { vm.setMetric(ChartViewModel.Metric.Pulse) },
                border = toggleMetricBorder(ChartViewModel.Metric.Pulse)
            ) {
                Text(
                    stringResource(
                        R.string.chart_metric_pulse,
                    ),
                )
            }
            OutlinedButton(
                onClick = { vm.setMetric(ChartViewModel.Metric.Weight) },
                border = toggleMetricBorder(ChartViewModel.Metric.Weight)
            ) {
                Text(
                    stringResource(
                        R.string.chart_metric_weight,
                    ),
                )
            }
            Box {
                OutlinedButton(
                    onClick = { rangeExpanded = true },
                ) {
                    Text(periodLabel)
                }
                DropdownMenu(
                    expanded = rangeExpanded,
                    onDismissRequest = { rangeExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.range_week)) },
                        onClick = {
                            vm.setRange(ChartViewModel.Range.Week); rangeExpanded =
                            false
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.range_month)) },
                        onClick = {
                            vm.setRange(ChartViewModel.Range.Month); rangeExpanded =
                            false
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.range_six_months)) },
                        onClick = {
                            vm.setRange(ChartViewModel.Range.SixMonths); rangeExpanded =
                            false
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.range_year)) },
                        onClick = {
                            vm.setRange(ChartViewModel.Range.Year); rangeExpanded =
                            false
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.range_all_time)) },
                        onClick = {
                            vm.setRange(ChartViewModel.Range.AllTime); rangeExpanded =
                            false
                        },
                    )
                }
            }
        }

        if (chartData.size < 2) {
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.chart_empty_hint),
                style = MaterialTheme.typography.bodyMedium
            )
            return
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 10.dp)
        ) {
            Text(
                "x${"%.1f".format(chartZoom)}",
                style = MaterialTheme.typography.bodySmall
            )
            if (state.metric == ChartViewModel.Metric.Bp) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier
                            .size(10.dp)
                            .background(PinkPrimary, CircleShape),
                    )
                    Text(
                        stringResource(R.string.chart_legend_systolic),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        Modifier
                            .size(10.dp)
                            .background(Color.Cyan, CircleShape),
                    )
                    Text(
                        stringResource(R.string.chart_legend_diastolic),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Text(
                stringResource(R.string.chart_showing_count, chartData.size),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        SimpleLineChart(
            metric = state.metric,
            range = state.range,
            measurements = chartData,
            weightDisplayUnit = weightDisplayUnit,
            chartZoom = chartZoom,
            yAxisLabel = yAxisLabel,
            axisColor = MaterialTheme.colorScheme.onSurfaceVariant,
            gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
            plotColor = MaterialTheme.colorScheme.onSurface,
        )

    }
}

@Composable
private fun SimpleLineChart(
    metric: ChartViewModel.Metric,
    range: ChartViewModel.Range,
    measurements: List<HealthMeasurement>,
    weightDisplayUnit: WeightUnit,
    chartZoom: Float,
    yAxisLabel: String,
    axisColor: Color,
    gridColor: Color,
    plotColor: Color,
) {

    val chartScroll = rememberScrollState()
    val isLandscape =
        LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    var chartContainerWidth by remember { mutableStateOf(370.dp) }
    if (isLandscape)
        chartContainerWidth = 750.dp
    val sorted = measurements.sortedBy { it.timestampEpochMillis }
    val zone = ZoneId.systemDefault()
    val locale = Locale.getDefault()
    val dateFormatter = dateTimeFormatterForRange(range, locale, true)
    val dateFormatterWithoutYear = dateTimeFormatterForRange(range, locale, false)

    fun valuesFor(m: HealthMeasurement): List<Double> =
        when (metric) {
            ChartViewModel.Metric.Bp -> listOf(
                m.systolic.toDouble(),
                m.diastolic.toDouble()
            )

            ChartViewModel.Metric.Pulse -> listOf(m.pulse!!.toDouble())
            ChartViewModel.Metric.Weight -> {
                val w = m.weight!!
                val v = when {
                    m.weightUnit == weightDisplayUnit -> w
                    m.weightUnit == WeightUnit.KG && weightDisplayUnit == WeightUnit.LB -> kgToPounds(
                        w
                    )

                    else -> poundsToKg(w)
                }
                listOf(v)
            }
        }

    val allValues = sorted.flatMap(::valuesFor)
    val minV = allValues.minOrNull() ?: 0.0
    val maxV = allValues.maxOrNull() ?: 1.0
    val pad = (maxV - minV).takeIf { it > 0 }?.times(0.1) ?: 1.0
    val minY = minV - pad
    val maxY = maxV + pad

    val minX = sorted.minOf { it.timestampEpochMillis }
    val maxX = sorted.maxOf { it.timestampEpochMillis }
    val rawSpan = (maxX - minX).toDouble().coerceAtLeast(86_400_000.0)
    val xPad = rawSpan * 0.04
    val plotMinX = minX - xPad
    val plotMaxX = maxX + xPad
    val xSpan = (plotMaxX - plotMinX).coerceAtLeast(1.0)

    val yStep = niceStep(maxY - minY, 6)
    var yTick = floor(minY / yStep) * yStep
    val yTicks = buildList {
        while (yTick <= maxY + yStep * 0.001 && size < 10) {
            add(yTick)
            yTick += yStep
        }
    }.ifEmpty { listOf(minY, maxY) }

    val xTickCountByPeriod = when (range) {
        ChartViewModel.Range.Week -> 7
        ChartViewModel.Range.Month -> 6
        ChartViewModel.Range.SixMonths -> 6
        ChartViewModel.Range.Year -> 6
        ChartViewModel.Range.AllTime -> 7
    }

    val xTickCount = if (chartZoom < 2) xTickCountByPeriod else xTickCountByPeriod * 2 - 1

    val xTicksMillis = List(xTickCount) { i ->
        val frac = if (xTickCount == 1) 0.0 else i.toDouble() / (xTickCount - 1)
        (plotMinX + xSpan * frac).toLong()
    }

    val stroke = Stroke(width = 4f, cap = StrokeCap.Round)
    val axisArgb = axisColor.toArgb()

    Row {
        Canvas(
            modifier = Modifier
                .height(370.dp)

        ) {
            val densityScale = density
            val labelPx = 11f * densityScale
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = labelPx
                color = axisArgb
            }
            val paintSmall = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = 10f * densityScale
                color = axisArgb
            }

            val reserveLeft = 35f * densityScale
            val reserveBottom = 25f * densityScale
            val reserveTop = 12 * densityScale

            val plotBottom = size.height - reserveBottom

            val plotH = plotBottom - reserveTop


            fun yAtValue(v: Double): Float {
                val t = ((v - minY) / (maxY - minY)).toFloat().coerceIn(0f, 1f)
                return plotBottom - plotH * t
            }
            // Y axis
            drawLine(
                color = axisColor,
                start = Offset(reserveLeft, reserveTop),
                end = Offset(reserveLeft, plotBottom),
                strokeWidth = 2f,
            )

            paint.textAlign = Paint.Align.RIGHT
            for (yt in yTicks) {
                val yy = yAtValue(yt)
                val label = if (metric == ChartViewModel.Metric.Weight) {
                    String.format(Locale.US, "%.1f", yt)
                } else {
                    String.format(Locale.US, "%.0f", yt)
                }
                drawContext.canvas.nativeCanvas.drawText(
                    label,
                    reserveLeft - 6f * densityScale,
                    yy + labelPx * 0.35f,
                    paint
                )
            }
            // Y-axis unit (rotated would be ideal; short label above chart)
            paint.textAlign = Paint.Align.LEFT
            drawContext.canvas.nativeCanvas.drawText(
                yAxisLabel,
                reserveLeft,
                reserveTop,
                paintSmall
            )
        }
        Box(
            modifier = Modifier
                .width(chartContainerWidth)
                .fillMaxHeight()
                .then(
                    if (chartZoom > 1f) {
                        Modifier.horizontalScroll(chartScroll)
                    } else {
                        Modifier
                    }
                )
        ) {
            Canvas(
                modifier = Modifier
                    .height(370.dp)
                    .width(chartContainerWidth * chartZoom),
            ) {
                val densityScale = density
                val labelPx = 11f * densityScale
                val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    textSize = labelPx
                    color = axisArgb
                }
                val paintSmall = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    textSize = 10f * densityScale
                    color = axisArgb
                }

                val reserveLeft = 35f * densityScale
                val reserveBottom = 25f * densityScale
                val reserveRight = 12f * densityScale
                val reserveTop = 12 * densityScale

                val plotLeft = reserveLeft
                val plotRight = size.width - reserveRight
                val plotTop = reserveTop
                val plotBottom = size.height - reserveBottom
                val plotW = plotRight - plotLeft
                val plotH = plotBottom - plotTop

                fun xAtMillis(millis: Long): Float {
                    val t = ((millis - plotMinX) / xSpan).toFloat().coerceIn(0f, 1f)
                    return plotLeft + plotW * t
                }

                fun yAtValue(v: Double): Float {
                    val t = ((v - minY) / (maxY - minY)).toFloat().coerceIn(0f, 1f)
                    return plotBottom - plotH * t
                }

                // Grid (horizontal)
                for (yt in yTicks) {
                    val yy = yAtValue(yt)
                    drawLine(
                        color = gridColor,
                        start = Offset(plotLeft, yy),
                        end = Offset(plotRight, yy),
                        strokeWidth = 1f,
                    )
                }
                // Grid (vertical)
                for (xm in xTicksMillis) {
                    val xx = xAtMillis(xm)
                    drawLine(
                        color = gridColor,
                        start = Offset(xx, plotTop),
                        end = Offset(xx, plotBottom),
                        strokeWidth = 1f,
                    )
                }

                // X axis
                drawLine(
                    color = axisColor,
                    start = Offset(plotLeft, plotBottom),
                    end = Offset(plotRight, plotBottom),
                    strokeWidth = 2f,
                )

                paint.textAlign = Paint.Align.CENTER
                for (xm in xTicksMillis) {
                    val xx = xAtMillis(xm)
                    val date = Instant.ofEpochMilli(xm).atZone(zone);
                    val sameYear = date.year == ZonedDateTime.now().year
                    val label =
                        date.format(if (sameYear) dateFormatterWithoutYear else dateFormatter)
                    drawContext.canvas.nativeCanvas.drawText(
                        label,
                        xx,
                        plotBottom + 16f * densityScale,
                        paintSmall
                    )
                }

                val seriesCount = if (metric == ChartViewModel.Metric.Bp) 2 else 1

                val colors = when (metric) {
                    ChartViewModel.Metric.Bp -> listOf(PinkPrimary, Color.Cyan)
                    ChartViewModel.Metric.Pulse -> listOf(PinkAccent)
                    ChartViewModel.Metric.Weight -> listOf(Orange)
                }

                repeat(seriesCount) { seriesIdx ->
                    val path = Path()
                    sorted.forEachIndexed { i, m ->
                        val v = valuesFor(m)[seriesIdx]
                        val pt = Offset(xAtMillis(m.timestampEpochMillis), yAtValue(v))
                        if (i == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
                    }
                    drawPath(
                        path = path,
                        color = colors[seriesIdx % colors.size],
                        style = stroke,
                    )
                }

                if (sorted.size <= 70) {
                    // Points
                    val pointSize = if (sorted.size > 30) 4f else 7f

                    repeat(seriesCount) { seriesIdx ->
                        for (m in sorted) {
                            val v = valuesFor(m)[seriesIdx]
                            val cx = xAtMillis(m.timestampEpochMillis)
                            val cy = yAtValue(v)
                            drawCircle(
                                color = colors[seriesIdx % colors.size],
                                radius = pointSize,
                                center = Offset(cx, cy),
                            )
                            drawCircle(
                                color = plotColor,
                                radius = pointSize,
                                center = Offset(cx, cy),
                                style = Stroke(width = 1.5f),
                            )
                        }
                    }
                }
            }
        }

    }
}


private fun dateTimeFormatterForRange(
    range: ChartViewModel.Range,
    locale: Locale,
    printYear: Boolean,
): DateTimeFormatter =
    when (range) {
        ChartViewModel.Range.Week,
        ChartViewModel.Range.Month,
            -> DateTimeFormatter.ofPattern("d MMM", locale)

        ChartViewModel.Range.SixMonths,
        ChartViewModel.Range.Year,
        ChartViewModel.Range.AllTime,
            -> if (printYear) DateTimeFormatter.ofPattern(
            "MMM yyyy",
            locale
        ) else DateTimeFormatter.ofPattern("MMM", locale)
    }

private fun niceStep(range: Double, maxTicks: Int): Double {
    if (range <= 0 || maxTicks < 1) return 1.0
    val rough = range / maxTicks
    val exp = floor(log10(rough))
    val mag = 10.0.pow(exp)
    val residual = rough / mag
    val nice = when {
        residual <= 1.0 -> 1.0
        residual <= 2.0 -> 2.0
        residual <= 5.0 -> 5.0
        else -> 10.0
    }
    return nice * mag
}

private fun filterByRange(
    measurements: List<HealthMeasurement>,
    range: ChartViewModel.Range,
): List<HealthMeasurement> {
    val days = when (range) {
        ChartViewModel.Range.Week -> 7
        ChartViewModel.Range.Month -> 30
        ChartViewModel.Range.SixMonths -> 180
        ChartViewModel.Range.Year -> 365
        ChartViewModel.Range.AllTime -> Int.MAX_VALUE
    }
    if (range == ChartViewModel.Range.AllTime) return measurements
    val cutoff = ZonedDateTime.now().minusDays(days.toLong()).toInstant().toEpochMilli()
    return measurements.filter { it.timestampEpochMillis >= cutoff }
}
