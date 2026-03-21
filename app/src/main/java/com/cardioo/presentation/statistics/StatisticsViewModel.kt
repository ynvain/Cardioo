package com.cardioo.presentation.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cardioo.domain.model.HealthMeasurement
import com.cardioo.domain.usecase.ObserveMeasurements
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    observeMeasurements: ObserveMeasurements,
) : ViewModel() {
    val state: StateFlow<State> =
        observeMeasurements()
            .map { measurements ->
                State(
                    measurements = measurements,
                    summary = Summary.from(measurements),
                )
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), State())

    data class State(
        val measurements: List<HealthMeasurement> = emptyList(),
        val summary: Summary = Summary(),
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
                return Summary(latest = latest, avgSystolic = avgSys, avgDiastolic = avgDia, count = list.size)
            }
        }
    }
}

