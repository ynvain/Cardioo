package com.cardioo.presentation.chart

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cardioo.presentation.theme.PinkPrimary
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

@RequiresApi(Build.VERSION_CODES.O)
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = { vm.setMetric(ChartViewModel.Metric.Bp) }) {
                Text(if (state.metric == ChartViewModel.Metric.Bp) "BP ✓" else "BP")
            }
            OutlinedButton(onClick = { vm.setMetric(ChartViewModel.Metric.Pulse) }) {
                Text(if (state.metric == ChartViewModel.Metric.Pulse) "Pulse ✓" else "Pulse")
            }
            OutlinedButton(onClick = { vm.setMetric(ChartViewModel.Metric.Weight) }) {
                Text(if (state.metric == ChartViewModel.Metric.Weight) "Weight ✓" else "Weight")
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = { vm.setRange(ChartViewModel.Range.Weekly) }) {
                Text(if (state.range == ChartViewModel.Range.Weekly) "Weekly ✓" else "Weekly")
            }
            OutlinedButton(onClick = { vm.setRange(ChartViewModel.Range.Monthly) }) {
                Text(if (state.range == ChartViewModel.Range.Monthly) "Monthly ✓" else "Monthly")
            }
        }

        if (chartData.size < 2) {
            Spacer(Modifier.height(8.dp))
            Text("Add more readings to see trends.", style = MaterialTheme.typography.bodyMedium)
            return
        }

        Text(
            when (state.metric) {
                ChartViewModel.Metric.Bp -> "Systolic/Diastolic over time"
                ChartViewModel.Metric.Pulse -> "Pulse over time"
                ChartViewModel.Metric.Weight -> "Weight over time"
            },
            style = MaterialTheme.typography.titleMedium,
        )

        SimpleLineChart(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            metric = state.metric,
            measurements = chartData,
        )

        Text(
            "Showing ${chartData.size} readings (${state.range.name.lowercase()}).",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SimpleLineChart(
    modifier: Modifier,
    metric: ChartViewModel.Metric,
    measurements: List<com.cardioo.domain.model.HealthMeasurement>,
) {
    val sorted = measurements.sortedBy { it.timestampEpochMillis }
    val xCount = sorted.size.coerceAtLeast(2)

    fun valuesFor(m: com.cardioo.domain.model.HealthMeasurement): List<Double> =
        when (metric) {
            ChartViewModel.Metric.Bp -> listOf(m.systolic.toDouble(), m.diastolic.toDouble())
            ChartViewModel.Metric.Pulse -> listOf(m.pulse!!.toDouble())
            ChartViewModel.Metric.Weight -> listOf(m.weight!!)
        }

    val allValues = sorted.flatMap(::valuesFor)
    val minV = allValues.minOrNull() ?: 0.0
    val maxV = allValues.maxOrNull() ?: 1.0
    val pad = (maxV - minV).takeIf { it > 0 }?.times(0.1) ?: 1.0
    val minY = minV - pad
    val maxY = maxV + pad

    val stroke = Stroke(width = 5f, cap = StrokeCap.Round)

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val left = 12f
        val right = w - 12f
        val top = 12f
        val bottom = h - 12f

        fun x(i: Int): Float =
            if (xCount == 1) left else (left + (right - left) * (i.toFloat() / (xCount - 1).toFloat()))

        fun y(v: Double): Float {
            val t = ((v - minY) / (maxY - minY)).toFloat().coerceIn(0f, 1f)
            return bottom - (bottom - top) * t
        }

        // axis baseline
        drawLine(
            color = Color.Cyan,
            start = Offset(left, bottom),
            end = Offset(right, bottom),
            strokeWidth = 2f,
        )

        val seriesCount = if (metric == ChartViewModel.Metric.Bp) 2 else 1
        val colors = listOf(PinkPrimary, Color.Cyan)

        repeat(seriesCount) { seriesIdx ->
            val path = Path()
            sorted.forEachIndexed { i, m ->
                val v = valuesFor(m)[seriesIdx]
                val pt = Offset(x(i), y(v))
                if (i == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
            }
            drawPath(
                path = path,
                color = colors[seriesIdx % colors.size],
                style = stroke,
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun filterByRange(
    measurements: List<com.cardioo.domain.model.HealthMeasurement>,
    range: ChartViewModel.Range,
): List<com.cardioo.domain.model.HealthMeasurement> {
    val days = if (range == ChartViewModel.Range.Weekly) 7 else 30
    val cutoff = ZonedDateTime.now().minusDays(days.toLong()).toInstant().toEpochMilli()
    return measurements.filter { it.timestampEpochMillis >= cutoff }
}

