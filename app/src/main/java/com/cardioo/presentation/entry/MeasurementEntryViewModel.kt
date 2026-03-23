package com.cardioo.presentation.entry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cardioo.domain.model.BpCategory
import com.cardioo.domain.model.HealthMeasurement
import com.cardioo.domain.model.HeightUnit
import com.cardioo.domain.model.UserProfile
import com.cardioo.domain.model.WeightUnit
import com.cardioo.domain.model.bpCategory
import com.cardioo.domain.model.cmToMeters
import com.cardioo.domain.model.displayName
import com.cardioo.domain.model.inchesToMeters
import com.cardioo.domain.model.kgToPounds
import com.cardioo.domain.model.poundsToKg
import com.cardioo.domain.model.toggle
import com.cardioo.domain.usecase.GetMeasurement
import com.cardioo.domain.usecase.ObserveProfile
import com.cardioo.domain.usecase.UpsertMeasurement
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class MeasurementEntryViewModel @Inject constructor(
    private val getMeasurement: GetMeasurement,
    private val upsertMeasurement: UpsertMeasurement,
    private val observeProfile: ObserveProfile,
) : ViewModel() {
    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state

    data class State(
        val loading: Boolean = true,
        val measurementId: Long? = null,
        val timestampEpochMillis: Long = System.currentTimeMillis(),
        val systolicText: String = "",
        val diastolicText: String = "",
        val pulseText: String = "",
        val weightText: String = "",
        val weightUnit: WeightUnit = WeightUnit.KG,
        val notes: String = "",
        val profile: UserProfile? = null,
        val error: String? = null,
        val saving: Boolean = false,
    ) {
        val systolic: Int? get() = systolicText.toIntOrNull()
        val diastolic: Int? get() = diastolicText.toIntOrNull()
        val pulse: Int? get() = pulseText.toIntOrNull()
        val weight: Double? get() = weightText.toDoubleOrNull()
    }

    fun load(measurementId: Long?) {
        viewModelScope.launch {
            val profile = observeProfile().first()
            _state.update { it.copy(profile = profile) }

            if (measurementId == null) {
                val defaultUnit = profile?.weightUnit ?: WeightUnit.KG
                _state.update { it.copy(loading = false, measurementId = null, weightUnit = defaultUnit) }
                return@launch
            }

            val m = getMeasurement(measurementId)
            if (m == null) {
                _state.update { it.copy(loading = false, measurementId = null, error = "Reading not found.") }
                return@launch
            }
            _state.update {
                it.copy(
                    loading = false,
                    measurementId = m.id,
                    timestampEpochMillis = m.timestampEpochMillis,
                    systolicText = m.systolic.toString(),
                    diastolicText = m.diastolic.toString(),
                    pulseText = m.pulse?.toString().orEmpty(),
                    weightText = m.weight?.let { w ->
                        if (w % 1.0 == 0.0) w.toInt().toString() else w.toString()
                    }.orEmpty(),
                    weightUnit = m.weightUnit,
                    notes = m.notes.orEmpty(),
                )
            }
        }
    }

    fun setTimestamp(epochMillis: Long) = _state.update { it.copy(timestampEpochMillis = epochMillis) }
    fun setSystolicText(v: String) = _state.update { it.copy(systolicText = v.filter(Char::isDigit)) }
    fun setDiastolicText(v: String) = _state.update { it.copy(diastolicText = v.filter(Char::isDigit)) }
    fun setPulseText(v: String) = _state.update { it.copy(pulseText = v.filter(Char::isDigit)) }
    fun setWeightText(v: String) = _state.update { it.copy(weightText = v.filter { c -> c.isDigit() || c == '.' }) }
    fun setNotes(v: String) = _state.update { it.copy(notes = v) }

    fun toggleWeightUnit() {
        _state.update { st ->
            val w = st.weight
            val newUnit = st.weightUnit.toggle()
            val newWeight =
                if (w == null) null else when (newUnit) {
                    WeightUnit.KG -> poundsToKg(w)
                    WeightUnit.LB -> kgToPounds(w)
                }
            st.copy(
                weightUnit = newUnit,
                weightText = newWeight?.let { "%.1f".format(it) } ?: st.weightText,
            )
        }
    }

    fun computedBpCategory(): BpCategory? {
        val s = _state.value.systolic ?: return null
        val d = _state.value.diastolic ?: return null
        return bpCategory(s, d)
    }

    /**
     * Used by the entry screen to auto-advance focus: only when the typed weight parses and is in the
     * realistic kg range (same rule as save validation), so partial input like "7" does not jump.
     */
    fun isWeightTextCompleteForFocus(weightText: String): Boolean {
        val w = weightText.trim().toDoubleOrNull() ?: return false
        if (w <= 0.0) return false
        val wKg = if (_state.value.weightUnit == WeightUnit.KG) w else poundsToKg(w)
        return wKg in 20.0..300.0
    }

    fun computedBmi(): Int? {
        val profile = _state.value.profile ?: return null
        val heightMeters =
            when (profile.heightUnit) {
                HeightUnit.CM -> cmToMeters(profile.height)
                HeightUnit.IN -> inchesToMeters(profile.height)
            }
        if (heightMeters <= 0.0) return null
        val weight = _state.value.weight ?: return null
        // BMI is calculated in kg/m², so we normalize the entered weight to kg first.
        val weightKg = if (_state.value.weightUnit == WeightUnit.KG) weight else poundsToKg(weight)
        val bmi = weightKg / (heightMeters * heightMeters)
        return bmi.roundToInt().takeIf { it in 1..200 }
    }

    fun validate(): String? {
        val s = _state.value.systolic ?: return "Enter systolic (90-180)."
        val d = _state.value.diastolic ?: return "Enter diastolic (60-120)."

        if (s !in 90..180) return "Systolic must be 90-180."
        if (d !in 60..120) return "Diastolic must be 60-120."

        val pulseText = _state.value.pulseText.trim()
        if (pulseText.isNotEmpty()) {
            val p = _state.value.pulse ?: return "Enter a valid pulse (40-200)."
            if (p !in 40..200) return "Pulse must be 40-200."
        }

        val weightText = _state.value.weightText.trim()
        if (weightText.isNotEmpty()) {
            val w = _state.value.weight ?: return "Enter a valid weight."
            if (w <= 0.0) return "Weight must be positive."
            val wKg = if (_state.value.weightUnit == WeightUnit.KG) w else poundsToKg(w)
            if (wKg !in 20.0..300.0) return "Weight looks unrealistic."
        }

        return null
    }

    fun save(onDone: () -> Unit) {
        val err = validate()
        if (err != null) {
            _state.update { it.copy(error = err) }
            return
        }

        val s = _state.value.systolic!!
        val d = _state.value.diastolic!!
        val p = _state.value.pulseText.trim().takeIf { it.isNotEmpty() }?.let { _state.value.pulse }
        val w = _state.value.weightText.trim().takeIf { it.isNotEmpty() }?.let { _state.value.weight }

        viewModelScope.launch {
            _state.update { it.copy(saving = true, error = null) }
            upsertMeasurement(
                HealthMeasurement(
                    id = _state.value.measurementId ?: 0L,
                    userId = _state.value.profile?.id ?: 0L,
                    timestampEpochMillis = _state.value.timestampEpochMillis,
                    systolic = s,
                    diastolic = d,
                    pulse = p,
                    weight = w,
                    weightUnit = _state.value.weightUnit,
                    notes = _state.value.notes.trim().ifBlank { null },
                ),
            )
            _state.update { it.copy(saving = false) }
            onDone()
        }
    }
}

