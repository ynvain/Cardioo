package com.cardioo.presentation.readings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cardioo.domain.model.HealthMeasurement
import com.cardioo.domain.model.UserProfile
import com.cardioo.domain.usecase.DeleteMeasurement
import com.cardioo.domain.usecase.ObserveMeasurements
import com.cardioo.domain.usecase.ObserveProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReadingsViewModel @Inject constructor(
    observeMeasurements: ObserveMeasurements,
    observeProfile: ObserveProfile,
    private val deleteMeasurement: DeleteMeasurement,
) : ViewModel() {
    private val refreshing = MutableStateFlow(false)

    val state: StateFlow<State> =
        combine(
            observeMeasurements(),
            observeProfile(),
            refreshing,
        ) { measurements, profile, isRefreshing ->
            State(
                measurements = measurements,
                profile = profile,
                isRefreshing = isRefreshing,
                summary = Summary.from(measurements),
            )
        }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5_000), State())

    data class State(
        val measurements: List<HealthMeasurement> = emptyList(),
        val profile: UserProfile? = null,
        val isRefreshing: Boolean = false,
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

    fun refresh() {
        viewModelScope.launch {
            refreshing.update { true }
            delay(450) // local DB, just UI affordance
            refreshing.update { false }
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch { deleteMeasurement(id) }
    }
}

