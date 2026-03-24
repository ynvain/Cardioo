package com.cardioo.presentation.chart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cardioo.domain.model.HealthMeasurement
import com.cardioo.domain.usecase.ObserveMeasurements
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class ChartViewModel @Inject constructor(
    observeMeasurements: ObserveMeasurements,
) : ViewModel() {
    private val metric = MutableStateFlow(Metric.Bp)
    private val range = MutableStateFlow(Range.Monthly)

    val state: StateFlow<State> =
        combine(observeMeasurements(), metric, range) { measurements, m, r ->
            State(
                metric = m,
                range = r,
                measurements = measurements,
            )
        }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5_000), State())

    data class State(
        val metric: Metric = Metric.Bp,
        val range: Range = Range.Monthly,
        val measurements: List<HealthMeasurement> = emptyList(),
    )

    enum class Metric { Bp, Pulse, Weight }
    enum class Range { Weekly, Monthly, SixMonths, Year }

    fun setMetric(v: Metric) = metric.update { v }
    fun setRange(v: Range) = range.update { v }
}

