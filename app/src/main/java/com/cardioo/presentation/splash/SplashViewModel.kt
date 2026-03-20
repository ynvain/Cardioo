package com.cardioo.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cardioo.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {
    private val _destination = MutableStateFlow<Destination?>(null)
    val destination: StateFlow<Destination?> = _destination

    init {
        viewModelScope.launch {
            val accounts = userRepository.observeAccounts().first()
            if (accounts.isEmpty()) {
                _destination.value = Destination.Onboarding
                return@launch
            }
            val current = userRepository.getCurrentAccountId()
            if (current == null || accounts.none { it.id == current }) {
                userRepository.setCurrentAccount(accounts.first().id)
            }
            _destination.value = Destination.Main
        }
    }

    enum class Destination { Onboarding, Main }
}

