package com.techlad.streakup.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.techlad.streakup.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SignUpUiState(
    val name: String = "",
    val gender: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val passwordVisible: Boolean = false,
    val confirmPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val navigateToLogin: Boolean = false,
)

class SignUpViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name, error = null)
    }

    fun updateGender(gender: String) {
        _uiState.value = _uiState.value.copy(gender = gender, error = null)
    }

    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email, error = null)
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

    fun onNavigatedToLogin() {
        _uiState.value = _uiState.value.copy(navigateToLogin = false)
    }

    fun submit() {
        val state = _uiState.value
        when {
            state.name.isBlank() -> {
                _uiState.value = state.copy(error = "Enter your name")
            }
            state.gender.isBlank() -> {
                _uiState.value = state.copy(error = "Select your gender")
            }
            state.email.isBlank() || !state.email.contains("@") -> {
                _uiState.value = state.copy(error = "Enter a valid email address")
            }
            state.password.length < 6 -> {
                _uiState.value = state.copy(error = "Password must be at least 6 characters")
            }
            state.password != state.confirmPassword -> {
                _uiState.value = state.copy(error = "Passwords do not match")
            }
            else -> viewModelScope.launch {
                _uiState.value = state.copy(isLoading = true, error = null)
                authRepository.signUp(
                    email = state.email.trim(),
                    password = state.password,
                    name = state.name.trim(),
                    gender = state.gender,
                ).fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(isLoading = false, success = true)
                    },
                    onFailure = { e ->
                        val message = e.message.orEmpty()
                        if (message.contains("Confirm your email", ignoreCase = true)) {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                navigateToLogin = true,
                                error = message,
                            )
                        } else {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = message.ifBlank { "Sign up failed" },
                            )
                        }
                    },
                )
            }
        }
    }
}
