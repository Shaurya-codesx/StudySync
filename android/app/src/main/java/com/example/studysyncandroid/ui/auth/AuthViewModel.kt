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

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data object Success : AuthUiState
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
                .onFailure { _loginState.value = AuthUiState.Error(it.message ?: "Login failed") }
        }
    }

    fun signup(email: String, password: String, displayName: String) {
        _signupState.value = AuthUiState.Loading
        viewModelScope.launch {
            authRepository.signup(email, password, displayName)
                .onSuccess { _signupState.value = AuthUiState.Success }
                .onFailure { _signupState.value = AuthUiState.Error(it.message ?: "Signup failed") }
        }
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onComplete()
        }
    }
}