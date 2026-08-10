package com.example.studysyncandroid.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studysyncandroid.data.remote.dto.UpcomingReviewDto
import com.example.studysyncandroid.data.repository.AnalyticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UpcomingReviewsViewModel @Inject constructor(
    private val analyticsRepository: AnalyticsRepository
) : ViewModel() {

    private val _upcomingReviews = MutableStateFlow<List<UpcomingReviewDto>>(emptyList())
    val upcomingReviews: StateFlow<List<UpcomingReviewDto>> = _upcomingReviews.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        fetchUpcomingReviews()
    }

    fun fetchUpcomingReviews() {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            analyticsRepository.getUpcomingReviews()
                .onSuccess { data ->
                    _upcomingReviews.value = data
                }
                .onFailure { error ->
                    _errorMessage.value = error.message ?: "Failed to load upcoming reviews"
                }
            _isLoading.value = false
        }
    }
}
