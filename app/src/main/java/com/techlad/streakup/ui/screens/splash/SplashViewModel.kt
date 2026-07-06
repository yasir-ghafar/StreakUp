package com.techlad.streakup.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.techlad.streakup.data.repository.AuthRepository
import com.techlad.streakup.data.repository.HabitRepository
import com.techlad.streakup.domain.model.AuthState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SplashViewModel(
    private val authRepository: AuthRepository,
    private val habitRepository: HabitRepository,
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.checkSession()
            val state = authRepository.authState.first()
            _authState.value = state
            if (state.isAuthenticated) {
                habitRepository.syncFromRemote()
                habitRepository.syncPendingToRemote()
            }
        }
    }
}
