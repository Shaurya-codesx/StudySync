package com.example.studysyncandroid.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studysyncandroid.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.studysyncandroid.util.toUserFriendlyMessage

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data object Success : AuthUiState
    data object RequiresVerification : AuthUiState
    data class Error(val message: String) : AuthUiState
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val loginState: StateFlow<AuthUiState> = _loginState.asStateFlow()

    private val _signupState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val signupState: StateFlow<AuthUiState> = _signupState.asStateFlow()

    fun login(email: String, password: String) {
        _loginState.value = AuthUiState.Loading
        viewModelScope.launch {
            authRepository.login(email, password)
                .onSuccess { _loginState.value = AuthUiState.Success }
                .onFailure { _loginState.value = AuthUiState.Error(it.toUserFriendlyMessage()) }
        }
    }

    fun signup(email: String, password: String, displayName: String) {
        _signupState.value = AuthUiState.Loading
        viewModelScope.launch {
            authRepository.signup(email, password, displayName)
                .onSuccess { _signupState.value = AuthUiState.RequiresVerification }
                .onFailure { _signupState.value = AuthUiState.Error(it.toUserFriendlyMessage()) }
        }
    }

    private val _verifyEmailState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val verifyEmailState: StateFlow<AuthUiState> = _verifyEmailState.asStateFlow()

    private val _resendState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val resendState: StateFlow<AuthUiState> = _resendState.asStateFlow()

    private val _forgotPasswordState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val forgotPasswordState: StateFlow<AuthUiState> = _forgotPasswordState.asStateFlow()

    private val _resetPasswordState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val resetPasswordState: StateFlow<AuthUiState> = _resetPasswordState.asStateFlow()

    fun verifyEmail(email: String, otp: String) {
        _verifyEmailState.value = AuthUiState.Loading
        viewModelScope.launch {
            authRepository.verifyEmail(email, otp)
                .onSuccess { _verifyEmailState.value = AuthUiState.Success }
                .onFailure { _verifyEmailState.value = AuthUiState.Error(it.toUserFriendlyMessage()) }
        }
    }

    fun resendVerification(email: String) {
        _resendState.value = AuthUiState.Loading
        viewModelScope.launch {
            authRepository.resendVerification(email)
                .onSuccess { _resendState.value = AuthUiState.Success }
                .onFailure { _resendState.value = AuthUiState.Error(it.toUserFriendlyMessage()) }
        }
    }

    fun forgotPassword(email: String) {
        _forgotPasswordState.value = AuthUiState.Loading
        viewModelScope.launch {
            authRepository.forgotPassword(email)
                .onSuccess { _forgotPasswordState.value = AuthUiState.Success }
                .onFailure { _forgotPasswordState.value = AuthUiState.Error(it.toUserFriendlyMessage()) }
        }
    }

    fun resetPassword(email: String, otp: String, newPassword: String) {
        _resetPasswordState.value = AuthUiState.Loading
        viewModelScope.launch {
            authRepository.resetPassword(email, otp, newPassword)
                .onSuccess { _resetPasswordState.value = AuthUiState.Success }
                .onFailure { _resetPasswordState.value = AuthUiState.Error(it.toUserFriendlyMessage()) }
        }
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onComplete()
        }
    }
}