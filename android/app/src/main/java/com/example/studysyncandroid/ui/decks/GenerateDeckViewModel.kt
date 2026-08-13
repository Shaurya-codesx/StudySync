package com.example.studysyncandroid.ui.decks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.studysyncandroid.data.repository.DeckRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.studysyncandroid.util.toUserFriendlyMessage

sealed interface GenerateDeckUiState {
    data object Idle : GenerateDeckUiState
    data object Loading : GenerateDeckUiState
    data class Success(val deckId: String) : GenerateDeckUiState
    data class Error(val message: String) : GenerateDeckUiState
}

@HiltViewModel
class GenerateDeckViewModel @Inject constructor(
    private val deckRepository: DeckRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<GenerateDeckUiState>(GenerateDeckUiState.Idle)
    val uiState: StateFlow<GenerateDeckUiState> = _uiState.asStateFlow()

    fun generateDeck(sourceText: String) {
        _uiState.value = GenerateDeckUiState.Loading
        viewModelScope.launch {
            deckRepository.generateDeck(sourceText)
                .onSuccess { deckId -> _uiState.value = GenerateDeckUiState.Success(deckId) }
                .onFailure { _uiState.value = GenerateDeckUiState.Error(it.toUserFriendlyMessage()) }
        }
    }
}