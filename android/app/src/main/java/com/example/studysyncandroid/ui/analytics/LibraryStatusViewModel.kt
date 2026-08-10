package com.example.studysyncandroid.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studysyncandroid.data.remote.dto.LibraryStatusDto
import com.example.studysyncandroid.data.repository.AnalyticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryStatusViewModel @Inject constructor(
    private val analyticsRepository: AnalyticsRepository
) : ViewModel() {

    private val _libraryStatus = MutableStateFlow<LibraryStatusDto?>(null)
    val libraryStatus: StateFlow<LibraryStatusDto?> = _libraryStatus.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        fetchLibraryStatus()
    }

    fun fetchLibraryStatus() {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            analyticsRepository.getLibraryStatus()
                .onSuccess { data ->
                    _libraryStatus.value = data
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "Failed to load library status"
                }
            _isLoading.value = false
        }
    }
}
