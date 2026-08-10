package com.example.studysyncandroid.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studysyncandroid.data.remote.dto.DailyRetentionDto
import com.example.studysyncandroid.data.repository.AnalyticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val analyticsRepository: AnalyticsRepository
) : ViewModel() {

    private val _retentionData = MutableStateFlow<List<DailyRetentionDto>>(emptyList())
    val retentionData: StateFlow<List<DailyRetentionDto>> = _retentionData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        fetchRetentionData()
    }

    fun fetchRetentionData() {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            analyticsRepository.getRetentionCurve()
                .onSuccess { data ->
                    _retentionData.value = data
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "Failed to load analytics"
                }
            _isLoading.value = false
        }
    }
}
