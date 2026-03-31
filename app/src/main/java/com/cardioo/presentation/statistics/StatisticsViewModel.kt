package com.cardioo.presentation.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cardioo.domain.model.BpCategory
import com.cardioo.domain.model.HealthMeasurement
import com.cardioo.domain.model.WeightUnit
import com.cardioo.domain.model.bpCategory
import com.cardioo.domain.usecase.ObserveMeasurements
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.ZonedDateTime
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    observeMeasurements: ObserveMeasurements,
) : ViewModel() {
    private val range = MutableStateFlow(Range.Month)

    val state: StateFlow<State> =
        combine(observeMeasurements(), range) { measurements, r ->
            val filtered = filterByRange(measurements, r)
            val summary = Summary.from(filtered)
            val table = TableStats.from(filtered)
            val avgCategory =
                if (summary.avgSystolic != null && summary.avgDiastolic != null) {
                    bpCategory(summary.avgSystolic, summary.avgDiastolic)
                } else {
                    null
                }
            val weightUnit = filtered.lastOrNull { it.weight != null }?.weightUnit ?: WeightUnit.KG
            val periodLabelRes = when (r) {
                Range.Week -> com.cardioo.R.string.range_week
                Range.Month -> com.cardioo.R.string.range_month
                Range.SixMonths -> com.cardioo.R.string.range_six_months
                Range.Year -> com.cardioo.R.string.range_year
                Range.AllTime -> com.cardioo.R.string.range_all_time
            }
            State(
                range = r,
                measurements = filtered,
                summary = summary,
                table = table,
                averageBpCategory = avgCategory,
                weightDisplayUnit = weightUnit,
                periodLabelRes = periodLabelRes,
            )
        }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), State())

    data class State(
        val range: Range = Range.Month,
        val measurements: List<HealthMeasurement> = emptyList(),
        val summary: Summary = Summary(),
        val table: TableStats = TableStats(),
        val averageBpCategory: BpCategory? = null,
        val weightDisplayUnit: WeightUnit = WeightUnit.KG,
        val periodLabelRes: Int = com.cardioo.R.string.range_month,
    )

    data class Summary(
        val latest: HealthMeasurement? = null,
        val avgSystolic: Int? = null,
        val avgDiastolic: Int? = null,
        val count: Int = 0,
    ) {
        companion object {
            fun from(list: List<HealthMeasurement>): Summary {
                val latest = list.firstOrNull()
                if (list.isEmpty()) return Summary(latest = null, count = 0)
                val avgSys = list.map { it.systolic }.average().toInt()
                val avgDia = list.map { it.diastolic }.average().toInt()
                return Summary(
                    latest = latest,
                    avgSystolic = avgSys,
                    avgDiastolic = avgDia,
                    count = list.size,
                )
            }
        }
    }

    data class TableStats(
        val minSystolic: Int? = null,
        val maxSystolic: Int? = null,
        val avgSystolic: Int? = null,
        val minDiastolic: Int? = null,
        val maxDiastolic: Int? = null,
        val avgDiastolic: Int? = null,
        val minPulse: Int? = null,
        val maxPulse: Int? = null,
        val avgPulse: Int? = null,
        val minWeight: Double? = null,
        val maxWeight: Double? = null,
        val avgWeight: Double? = null,
    ) {
        companion object {
            fun from(list: List<HealthMeasurement>): TableStats {
                if (list.isEmpty()) return TableStats()
                val pulses = list.mapNotNull { it.pulse }
                val weights = list.mapNotNull { it.weight }
                return TableStats(
                    minSystolic = list.minOfOrNull { it.systolic },
                    maxSystolic = list.maxOfOrNull { it.systolic },
                    avgSystolic = list.map { it.systolic }.average().toInt(),
                    minDiastolic = list.minOfOrNull { it.diastolic },
                    maxDiastolic = list.maxOfOrNull { it.diastolic },
                    avgDiastolic = list.map { it.diastolic }.average().toInt(),
                    minPulse = pulses.minOrNull(),
                    maxPulse = pulses.maxOrNull(),
                    avgPulse = pulses.takeIf { it.isNotEmpty() }?.average()?.toInt(),
                    minWeight = weights.minOrNull(),
                    maxWeight = weights.maxOrNull(),
                    avgWeight = weights.takeIf { it.isNotEmpty() }?.average(),
                )
            }
        }
    }

    enum class Range { Week, Month, SixMonths, Year, AllTime }

    fun setRange(v: Range) = range.update { v }

    private fun filterByRange(
        measurements: List<HealthMeasurement>,
        range: Range,
    ): List<HealthMeasurement> {
        if (range == Range.AllTime) return measurements
        val days = when (range) {
            Range.Week -> 7
            Range.Month -> 30
            Range.SixMonths -> 180
            Range.Year -> 365
            Range.AllTime -> Int.MAX_VALUE
        }
        val cutoff = ZonedDateTime.now().minusDays(days.toLong()).toInstant().toEpochMilli()
        return measurements.filter { it.timestampEpochMillis >= cutoff }
    }
}

