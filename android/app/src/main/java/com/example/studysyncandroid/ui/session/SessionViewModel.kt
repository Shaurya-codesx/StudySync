package com.example.studysyncandroid.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studysyncandroid.data.repository.AuthRepository
import com.example.studysyncandroid.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination: StateFlow<String?> = _startDestination.asStateFlow()

    init {
        viewModelScope.launch {
            _startDestination.value = if (authRepository.isLoggedIn()) {
                Screen.DeckList.route
            } else {
                Screen.Login.route
            }
        }
    }
}