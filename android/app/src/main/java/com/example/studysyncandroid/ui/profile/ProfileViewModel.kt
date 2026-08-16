package com.example.studysyncandroid.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studysyncandroid.data.remote.dto.UserProfileResponse
import com.example.studysyncandroid.data.repository.AuthRepository
import com.example.studysyncandroid.util.toUserFriendlyMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class Success(val profile: UserProfileResponse) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _profileState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val profileState: StateFlow<ProfileUiState> = _profileState.asStateFlow()

    private val _resetOtpState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    // We can just reuse a boolean or string state for the OTP sending
    private val _otpSent = MutableStateFlow<Boolean>(false)
    val otpSent: StateFlow<Boolean> = _otpSent.asStateFlow()

    private val _accountDeleted = MutableStateFlow<Boolean>(false)
    val accountDeleted: StateFlow<Boolean> = _accountDeleted.asStateFlow()

    init {
        fetchProfile()
    }

    fun fetchProfile() {
        _profileState.value = ProfileUiState.Loading
        viewModelScope.launch {
            authRepository.getUserProfile()
                .onSuccess { profile ->
                    _profileState.value = ProfileUiState.Success(profile)
                }
                .onFailure { error ->
                    _profileState.value = ProfileUiState.Error(error.toUserFriendlyMessage())
                }
        }
    }

    fun sendResetOtp(email: String) {
        viewModelScope.launch {
            authRepository.forgotPassword(email)
                .onSuccess {
                    _otpSent.value = true
                }
                .onFailure {
                    // Could handle error state if needed, but for simplicity
                }
        }
    }
    
    fun resetOtpState() {
        _otpSent.value = false
    }

    fun updateDisplayName(newName: String) {
        viewModelScope.launch {
            authRepository.updateUserProfile(newName)
                .onSuccess {
                    fetchProfile() // Refresh the profile to get updated info
                }
                .onFailure { error ->
                    _profileState.value = ProfileUiState.Error(error.toUserFriendlyMessage())
                }
        }
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            onComplete()
        }
    }

    fun deleteAccount(password: String) {
        viewModelScope.launch {
            authRepository.deleteAccount(password)
                .onSuccess {
                    authRepository.logout()
                    _accountDeleted.value = true
                }
                .onFailure { error ->
                    _profileState.value = ProfileUiState.Error(error.toUserFriendlyMessage())
                }
        }
    }
}
