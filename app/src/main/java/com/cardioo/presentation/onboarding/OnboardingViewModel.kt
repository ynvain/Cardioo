package com.cardioo.presentation.onboarding

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cardioo.R
import com.cardioo.domain.model.Gender
import com.cardioo.domain.model.HeightUnit
import com.cardioo.domain.model.UserProfile
import com.cardioo.domain.model.WeightUnit
import com.cardioo.domain.model.toggle
import com.cardioo.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userRepository: UserRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(State())
    val state: StateFlow<State> = _state

    data class State(
        val name: String = "",
        val heightText: String = "",
        val heightUnit: HeightUnit = HeightUnit.CM,
        val weightUnit: WeightUnit = WeightUnit.KG,
        val dateOfBirth: LocalDate? = null,
        val gender: Gender? = null,
        val error: String? = null,
        val saving: Boolean = false,
    )

    fun setName(v: String) = _state.update { it.copy(name = v) }
    fun setHeightText(v: String) = _state.update { it.copy(heightText = v.filter { c -> c.isDigit() || c == '.' }) }
    fun toggleHeightUnit() = _state.update { it.copy(heightUnit = it.heightUnit.toggle()) }
    fun toggleWeightUnit() = _state.update { it.copy(weightUnit = it.weightUnit.toggle()) }
    fun setDob(v: LocalDate?) = _state.update { it.copy(dateOfBirth = v) }
    fun setGender(v: Gender?) = _state.update { it.copy(gender = v) }

    fun save(onDone: () -> Unit) {
        val height = _state.value.heightText.toDoubleOrNull()
        val name = _state.value.name.trim()
        if (name.isBlank()) {
            _state.update { it.copy(error = context.getString(R.string.error_account_name_required)) }
            return
        }
        if (height == null || height <= 0.0) {
            _state.update { it.copy(error = context.getString(R.string.error_height_required)) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(saving = true, error = null) }
            userRepository.createAccount(
                UserProfile(
                    name = name,
                    height = height,
                    heightUnit = _state.value.heightUnit,
                    weightUnit = _state.value.weightUnit,
                    dateOfBirth = _state.value.dateOfBirth,
                    gender = _state.value.gender,
                ),
            )
            _state.update { it.copy(saving = false) }
            onDone()
        }
    }
}
