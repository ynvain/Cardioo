package com.cardioo.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cardioo.domain.model.HeightUnit
import com.cardioo.domain.model.UserProfile
import com.cardioo.domain.model.WeightUnit
import com.cardioo.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {
    val state: StateFlow<State> =
        combine(
            userRepository.observeAccounts(),
            userRepository.observeProfile(),
        ) { accounts, current ->
            State(accounts = accounts, currentAccountId = current?.id)
        }.stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5_000), State())

    data class State(
        val accounts: List<UserProfile> = emptyList(),
        val currentAccountId: Long? = null,
    )

    fun switchAccount(id: Long) {
        viewModelScope.launch { userRepository.setCurrentAccount(id) }
    }

    fun createAccount(name: String) {
        viewModelScope.launch {
            val cleanName = name.trim()
            if (cleanName.isBlank()) return@launch
            val fallbackUnit = state.value.accounts.firstOrNull()?.weightUnit ?: WeightUnit.KG
            userRepository.createAccount(
                UserProfile(
                    name = cleanName,
                    height = 170.0,
                    heightUnit = HeightUnit.CM,
                    weightUnit = fallbackUnit,
                    dateOfBirth = null,
                    gender = null,
                ),
            )
        }
    }
}

