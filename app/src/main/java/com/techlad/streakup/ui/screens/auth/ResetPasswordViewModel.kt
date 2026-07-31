package com.techlad.streakup.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.techlad.streakup.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ResetPasswordUiState(
    val password: String = "",
    val confirmPassword: String = "",
    val passwordVisible: Boolean = false,
    val confirmPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
)

class ResetPasswordViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResetPasswordUiState())
    val uiState: StateFlow<ResetPasswordUiState> = _uiState.asStateFlow()

    init {
        if (!authRepository.hasRecoverySession()) {
            _uiState.value = _uiState.value.copy(
                error = "Open the password reset link from your email first.",
            )
        }
    }

    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(password = password, error = null)
    }

    fun updateConfirmPassword(confirmPassword: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = confirmPassword, error = null)
    }

    fun togglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(passwordVisible = !_uiState.value.passwordVisible)
    }

    fun toggleConfirmPasswordVisibility() {
        _uiState.value = _uiState.value.copy(
            confirmPasswordVisible = !_uiState.value.confirmPasswordVisible,
        )
    }

    fun submit() {
        val state = _uiState.value
        when {
            !authRepository.hasRecoverySession() -> {
                _uiState.value = state.copy(error = "Reset session expired. Request a new link.")
            }
            state.password.length < 6 -> {
                _uiState.value = state.copy(error = "Password must be at least 6 characters")
            }
            state.password != state.confirmPassword -> {
                _uiState.value = state.copy(error = "Passwords do not match")
            }
            else -> viewModelScope.launch {
                _uiState.value = state.copy(isLoading = true, error = null)
                authRepository.updatePassword(state.password).fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(isLoading = false, success = true)
                    },
                    onFailure = { e ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = e.message ?: "Could not update password",
                        )
                    },
                )
            }
        }
    }
}
