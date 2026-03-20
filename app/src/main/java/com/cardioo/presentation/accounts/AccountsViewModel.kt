package com.cardioo.presentation.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cardioo.domain.model.UserProfile
import com.cardioo.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountsViewModel @Inject constructor(
    private val userRepository: UserRepository,
) : ViewModel() {
    val state: StateFlow<State> =
        combine(
            userRepository.observeAccounts(),
            userRepository.observeProfile(),
        ) { accounts, current ->
            State(accounts = accounts, currentId = current?.id)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), State())

    data class State(
        val accounts: List<UserProfile> = emptyList(),
        val currentId: Long? = null,
    )

    fun switchTo(id: Long) {
        viewModelScope.launch { userRepository.setCurrentAccount(id) }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            if (state.value.accounts.size <= 1) return@launch
            userRepository.deleteAccount(id)
        }
    }
}

